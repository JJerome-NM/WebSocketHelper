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

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SocketApplication extends TextWebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketApplication.class);

    private static final int THREAD_POOL = Runtime.getRuntime().availableProcessors();

    private static MessageSender messageSender;

    private final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL);

    private final Map<String, WebSocketSession> allSession = new HashMap<>();

    private final Map<String, BiConsumer<WebSocketSession, TextMessage>> methodMappings = new HashMap<>();

    private final List<Consumer<WebSocketSession>> connectionMappings = new ArrayList<>();

    private final List<BiConsumer<WebSocketSession, CloseStatus>> disconnectMappings = new ArrayList<>();

    private final Set<SocketConnectionFilter> connectionFilters = new TreeSet<>(new FiltersComparator<>());

    private final Set<SocketMessageFilter> messageFilters = new TreeSet<>(new FiltersComparator<>());

    private final SocketControllersContext controllersContext = new SocketControllersContext();

    private final RequestAccepter requestAccepter  = new RequestAccepter(
            this.methodMappings, this.messageFilters, this.executorService);




    public SocketApplication(Class<?> runningClass){
        messageSender = new MessageSender(this.allSession, this.executorService);

        this.addSocketControllers(runningClass);

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
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(SocketController.class);
        classes.forEach(this::addSocketController);

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

    public static MessageSender getMessageSender() {
        return messageSender;
    }
}
