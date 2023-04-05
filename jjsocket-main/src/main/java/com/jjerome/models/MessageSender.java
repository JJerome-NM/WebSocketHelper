package com.jjerome.models;

import com.jjerome.dto.Response;
import com.jjerome.mappers.ResponseMapper;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;

@AllArgsConstructor
public class MessageSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageSender.class);

    private final Map<String, WebSocketSession> allSession;

    private final ExecutorService executorService;

    public <RB> void send(String sessionID, String responsePath, RB message){
        this.send(sessionID, new Response<>(responsePath, message));
    }

    public void send(String sessionID, Response<?> response){
        executorService.submit(() -> {
            if (!this.allSession.containsKey(sessionID)){
                LOGGER.error("Send a message to an unidentified session");
                return;
            }
            try{
                this.allSession.get(sessionID).sendMessage(new TextMessage(ResponseMapper.toJSON(response)));
            } catch (IOException exception){
                LOGGER.error(exception.getMessage());
            }
        });
    }

    public <RB> void sendToGroup(String[] sessionsID, String responsePath, RB message){
        for (String sessionID : sessionsID){
            this.send(sessionID, new Response<>(responsePath, message));
        }
    }
}
