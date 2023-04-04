package com.jjerome.models;

import com.jjerome.annotations.SocketComponentsScan;
import com.jjerome.annotations.SocketController;
import com.jjerome.context.SocketControllersContext;
import com.jjerome.dto.Response;
import lombok.NonNull;
import org.reflections.Reflections;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@Component
public class SocketApplication extends TextWebSocketHandler {

    private final ExecutorService executorService = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors());

    private final Map<String, WebSocketSession> allSession = new HashMap<>();

    private final Map<String, BiFunction<WebSocketSession, TextMessage, Response<?>>> methodMappings = new HashMap<>();

    private final List<Consumer<WebSocketSession>> connectionMappings = new ArrayList<>();

    private final List<BiConsumer<WebSocketSession, CloseStatus>> disconnectMappings = new ArrayList<>();

    private final SocketControllersContext controllersContext = new SocketControllersContext();

    private final List<SocketConnectionFilter> connectionFilters = new ArrayList<>();

    private final List<SocketMessageFilter> messageFilters = new ArrayList<>();

    private final Map<String, SocketMethodFilter> methodFilters = new HashMap<>();

    private final SocketRequestAccepter requestAccepter;



    public SocketApplication(Class<?> runningClass){
        this.addSocketControllers(runningClass);

        this.requestAccepter = new SocketRequestAccepter(this.methodMappings, this.messageFilters,
                this.executorService);

        System.out.println("SocketApplication started");
    }



    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        executorService.submit(() -> {
            for (SocketConnectionFilter filter : this.connectionFilters){
                if (!filter.doFilter(session)){
                    try {
                        Response<?> response = new Response<>("/error/filter",
                                "Filtering failed", 406);
                        session.sendMessage(new TextMessage(ResponseMapper.toJSON(response)));
                        session.close();
                        return;
                    } catch (IOException exception){
                        System.out.println(exception.getMessage());
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
        this.executorService.submit(() -> this.disconnectMappings
                .forEach(map -> map.accept(session, status)));

        this.allSession.remove(session.getId());
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
            this.messageFilters.add(filterClass.getDeclaredConstructor().newInstance());
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                 | InvocationTargetException exception){
            System.out.println(exception.getMessage());
        }
    }

    private void addSocketConnectionFilters(Class<? extends SocketConnectionFilter> filterClass){
        try {
            this.connectionFilters.add(filterClass.getDeclaredConstructor().newInstance());
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                 | InvocationTargetException exception){
            System.out.println(exception.getMessage());
        }
    }

    private void addSocketController(Class<?> controllerClass){
        if (!controllerClass.isAnnotationPresent(SocketController.class)){
            throw new RuntimeException(controllerClass + " this class is not a Socket Controller");
        }

        this.connectionMappings.addAll(this.controllersContext.addConnectionMappings(controllerClass));
        this.disconnectMappings.addAll(this.controllersContext.addDisconnectMappings(controllerClass));
        this.methodMappings.putAll(this.controllersContext.addRequestsMappings(controllerClass));

        System.out.println(controllerClass.getName() + " class added");
    }
}