package com.jjerome.models;

import com.jjerome.dto.Response;
import org.springframework.http.HttpStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

public class SocketRequestAccepter implements Runnable {

    private final WebSocketSession socketSession;

    private final Map<String, Function<TextMessage, Response<?>>> socketMappings;

    private final String requestPath;

    private final TextMessage textMessage;



    SocketRequestAccepter(WebSocketSession socketSession,
                          Map<String, Function<TextMessage, Response<?>>> socketMappings,
                          String requestPath, TextMessage textMessage){
        this.socketSession = socketSession;
        this.socketMappings = socketMappings;
        this.requestPath = requestPath;
        this.textMessage = textMessage;
    }

    @Override
    public void run() {
        try{
            if (this.socketMappings.containsKey(this.requestPath)){
                Response<?> response = this.socketMappings.get(this.requestPath).apply(this.textMessage);

                this.socketSession.sendMessage(new TextMessage(response.toJSON()));
            } else {
                this.socketSession.sendMessage(new TextMessage(new Response<>(
                        "/error", "Mapping not found", HttpStatus.BAD_REQUEST).toJSON()));
            }
        } catch (IOException exception){
            System.out.println(exception.getMessage());
        }
    }
}
