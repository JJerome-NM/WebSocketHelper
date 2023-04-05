package com.jjerome.models;

import com.jjerome.annotations.SocketComponentsScan;
import com.jjerome.annotations.SocketController;
import com.jjerome.context.SocketControllersContext;
import com.jjerome.dto.Response;
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

public class SocketApplication extends TextWebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketApplication.class);

    private final ExecutorService executorService;

    private final Map<String, WebSocketSession> allSession;

    private final Map<String, BiConsumer<WebSocketSession, TextMessage>> methodMappings = new HashMap<>();

    private final List<Consumer<WebSocketSession>> connectionMappings = new ArrayList<>();

    private final List<BiConsumer<WebSocketSession, CloseStatus>> disconnectMappings = new ArrayList<>();

    private final Set<SocketConnectionFilter> connectionFilters = new TreeSet<>(new FiltersComparator<>());

    private final Set<SocketMessageFilter> messageFilters = new TreeSet<>(new FiltersComparator<>());

    private final SocketControllersContext controllersNewContext;

    private final ApplicationContext applicationContext;

    private final RequestAccepter requestAccepter;

    private final MessageSender messageSender;

    private final BeanUtil beanUtil;

    public SocketApplication(SocketControllersContext controllersNewContext,
                                ApplicationContext applicationContext,
                                BeanUtil beanUtil, ExecutorService executorService,
                                Map<String, WebSocketSession> allSessions,
                                MessageSender messageSender){

        this.controllersNewContext = controllersNewContext;
        this.applicationContext = applicationContext;
        this.beanUtil = beanUtil;
        this.executorService = executorService;
        this.allSession = allSessions;
        this.messageSender = messageSender;
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
                    this.closeSession(session, ResponseErrors.FILTERING_FAIL.getResponse());
                    return;
                }
            }
            this.allSession.put(session.getId(), session);
            this.connectionMappings.forEach(mapping -> mapping.accept(session));
        });
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        this.allSession.remove(session.getId());
        this.executorService.submit(() -> this.disconnectMappings.forEach(map -> map.accept(session, status)));
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
        this.requestAccepter.acceptMessage(session, message);
    }

    private void closeSession(WebSocketSession session, Response<?> closeResponse){
        try {
            session.sendMessage(new TextMessage(ResponseMapper.toJSON(closeResponse)));
            session.close();
        } catch (IOException exception){
            LOGGER.error(exception.getMessage());
        }
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
        this.messageFilters.add(this.applicationContext.getBean(filterClass));
    }

    private void addSocketConnectionFilters(Class<? extends SocketConnectionFilter> filterClass){
        this.connectionFilters.add(this.applicationContext.getBean(filterClass));
    }

    private void addSocketController(Class<?> controllerClass){
        if (!controllerClass.isAnnotationPresent(SocketController.class)){
            throw new RuntimeException(controllerClass + " this class is not a Socket Controller");
        }

        this.connectionMappings.addAll(this.controllersNewContext.findConnectionMappings(controllerClass));
        this.disconnectMappings.addAll(this.controllersNewContext.findDisconnectMappings(controllerClass));
        this.methodMappings.putAll(this.controllersNewContext.findRequestMappings(controllerClass));

        LOGGER.info(controllerClass.getName() + " controller added successfully");
    }
}
