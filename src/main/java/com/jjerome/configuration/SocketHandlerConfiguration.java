package com.jjerome.configuration;

import com.jjerome.models.SocketApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

public class SocketHandlerConfiguration implements WebSocketConfigurer {

    @Autowired
    private SocketApplication socketApplication;

    @Value("${socket.path:/socket}")
    private String PATH;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(socketApplication, PATH);
    }
}
