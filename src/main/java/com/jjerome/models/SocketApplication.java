package com.jjerome.models;

import com.jjerome.annotations.FilteringOrder;
import com.jjerome.annotations.SocketComponentsScan;
import com.jjerome.annotations.SocketController;
import com.jjerome.context.SocketControllersContext;
import com.jjerome.dto.Response;
import com.jjerome.exceptions.ExceptionMessage;
import com.jjerome.filters.FiltersComparator;
import com.jjerome.filters.SocketConnectionFilter;
import com.jjerome.filters.SocketMessageFilter;
import com.jjerome.mappers.ResponseMapper;

import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

//@Component
public class SocketApplication extends TextWebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketApplication.class);

    private final ExecutorService executorService;

    private final Map<String, WebSocketSession> allSession;

    private final Map<String, BiConsumer<WebSocketSession, TextMessage>> methodMappings = new HashMap<>();

    private final List<Consumer<WebSocketSession>> connectionMappings = new ArrayList<>();

    private final List<BiConsumer<WebSocketSession, CloseStatus>> disconnectMappings = new ArrayList<>();

    private final Set<SocketConnectionFilter> connectionFilters = new TreeSet<>(new FiltersComparator<>());

    private final Set<SocketMessageFilter> messageFilters = new TreeSet<>(new FiltersComparator<>());

    private final SocketControllersContext controllersContext;

    private final ApplicationContext applicationContext;

    private final RequestAccepter requestAccepter;

    private final BeanUtil beanUtil;

    public SocketApplication(SocketControllersContext socketControllersContext,
                                ApplicationContext applicationContext,
                                BeanUtil beanUtil, ExecutorService executorService,
                                Map<String, WebSocketSession> allSessions,
                                MessageSender messageSender){

        this.controllersContext = socketControllersContext;
        this.applicationContext = applicationContext;
        this.beanUtil = beanUtil;
        this.executorService = executorService;
        this.allSession = allSessions;
        this.requestAccepter = new RequestAccepter(messageSender, this.methodMappings, this.messageFilters,
                executorService);
    }

    @PostConstruct
    public void started(){
        beanUtil.findSpringBootApplicationBeanClass().forEach(this::addSocketControllers);
        LOGGER.info("SocketApplication started");
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        executorService.submit(() -> {
            for (SocketConnectionFilter filter : this.connectionFilters){
                if (!filter.doFilter(session)){
                    try {
                        Response<?> response = ResponseErrors.FILTERING_FAIL.getResponse();
                        session.sendMessage(new TextMessage(ResponseMapper.toJSON(response)));
                        session.close();
                        return;
                    } catch (IOException exception){
                        LOGGER.error(exception.getMessage());
                        return;
                    }
                }
            }
            this.connectionMappings.forEach(mapping -> mapping.accept(session));

            this.allSession.put(session.getId(), session);
        });
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        this.allSession.remove(session.getId());
        this.executorService.submit(() -> this.disconnectMappings
                .forEach(map -> map.accept(session, status)));
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
        this.requestAccepter.acceptMessage(session, message);
    }

    private void addSocketControllers(Class<?> runningClass){

        Reflections reflections = new Reflections(runningClass.getPackageName());

        reflections.getTypesAnnotatedWith(SocketController.class).forEach(this::addSocketController);


        if (!runningClass.isAnnotationPresent(SocketComponentsScan.class)) return;

        for(String pack : runningClass.getAnnotation(SocketComponentsScan.class).packages()){
            reflections = new Reflections(pack);

            reflections.getTypesAnnotatedWith(SocketController.class).forEach(this::addSocketController);
            reflections.getSubTypesOf(SocketMessageFilter.class).forEach(this::addSocketMessageFilters);
            reflections.getSubTypesOf(SocketConnectionFilter.class).forEach(this::addSocketConnectionFilters);
        }
    }

    private void addSocketMessageFilters(Class<? extends SocketMessageFilter> filterClass){
        try {
            if (!filterClass.isAnnotationPresent(FilteringOrder.class)){
                LOGGER.warn(filterClass.getName() + " " + ExceptionMessage.CLASS_DONT_HAVE_FILTERING_ORDER.getMessage());
            }
            this.messageFilters.add(filterClass.getDeclaredConstructor().newInstance());
        } catch (ReflectiveOperationException exception){
            LOGGER.error(exception.getMessage());
        }
    }

    private void addSocketConnectionFilters(Class<? extends SocketConnectionFilter> filterClass){
        try {
            if (!filterClass.isAnnotationPresent(FilteringOrder.class)){
                LOGGER.warn(filterClass.getName() + " " + ExceptionMessage.CLASS_DONT_HAVE_FILTERING_ORDER.getMessage());
            }
            this.connectionFilters.add(filterClass.getDeclaredConstructor().newInstance());
        } catch (ReflectiveOperationException exception){
            LOGGER.error(exception.getMessage());
        }
    }

    private void addSocketController(Class<?> controllerClass){
        if (!controllerClass.isAnnotationPresent(SocketController.class)){
            throw new RuntimeException(controllerClass + " this class is not a Socket Controller");
        }

        this.connectionMappings.addAll(this.controllersContext.addConnectionMappings(controllerClass));
        this.disconnectMappings.addAll(this.controllersContext.addDisconnectMappings(controllerClass));
        this.methodMappings.putAll(this.controllersContext.addRequestsMappings(controllerClass));

        LOGGER.info(controllerClass.getName() + " controller added successfully");
    }
}
