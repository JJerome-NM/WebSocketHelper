package com.jjerome.models;

import com.jjerome.annotations.SocketController;
import com.jjerome.context.SocketContext;
import com.jjerome.dto.Response;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.reflections.Reflections;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class SocketApplication extends TextWebSocketHandler {

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    private final Map<String, WebSocketSession> allSession = new HashMap<>();

    private final Map<String, Function<TextMessage, Response<?>>> socketMappings = new HashMap<>();

    private final List<Consumer<WebSocketSession>> connectionMappings = new ArrayList<>();

    private final List<BiConsumer<WebSocketSession, CloseStatus>> disconnectMappings = new ArrayList<>();

    private final SocketContext socketContext = new SocketContext();

//  ------------------------------------------- Constructors

    public SocketApplication(String packageName){
        this.run(packageName);
        System.out.println("SocketApplication started");
    }


//  ------------------------------------------- WebSocket methods

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) {
        this.allSession.put(session.getId(), session);

        for (Consumer<WebSocketSession> connectionMapping : this.connectionMappings) {
            connectionMapping.accept(session);
        }
    }

    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus status) {
        for(BiConsumer<WebSocketSession, CloseStatus> disconnectMapping : this.disconnectMappings){
            disconnectMapping.accept(session, status);
        }

        this.allSession.remove(session.getId());
//        System.out.println(allSession);
    }

    @Override
    protected void handleTextMessage(@NotNull WebSocketSession session, @NotNull TextMessage message) {

        JSONObject jsonObject = new JSONObject(message.getPayload());

        if (jsonObject.has("requestPath")){
            String requestPath = jsonObject.getString("requestPath");

            this.executorService.submit(new SocketRequestAccepter(session, this.socketMappings, requestPath, message));
        } else {
            try {
                session.sendMessage(new TextMessage(new Response<>("/error/json/request-path",
                        "Request path is null", HttpStatus.BAD_REQUEST).toJSON()));
            } catch (IOException exception){
                System.out.println(exception.getMessage());
            }
        }
    }



//  ------------------------------------------- This class methods

    private void run(String packageName){
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(SocketController.class);

        classes.forEach(this::addSocketController);
//        this.startAcceptSockets();
    }

    private void addSocketController(Class<?> controllerClass){
        if (!controllerClass.isAnnotationPresent(SocketController.class)){
            throw new RuntimeException(controllerClass + " this class is not a Socket Controller");
        }

        this.connectionMappings.addAll(this.socketContext.addConnectionMappings(controllerClass));
        this.disconnectMappings.addAll(this.socketContext.addDisconnectMappings(controllerClass));
        this.socketMappings.putAll(this.socketContext.addRequestsMappings(controllerClass));

        System.out.println(controllerClass.getName() + " class added");
    }
}
