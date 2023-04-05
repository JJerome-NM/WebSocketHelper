package com.jjerome.configuration;

import com.jjerome.context.SocketControllersContext;
import com.jjerome.models.BeanUtil;
import com.jjerome.models.MessageSender;
import com.jjerome.models.SocketApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@ComponentScan({"com.jjerome.models", "com.jjerome.context"})
@EnableWebSocket
public class SocketConfiguration{

    private static final int THREAD_POOL = Runtime.getRuntime().availableProcessors();

    private final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(THREAD_POOL);
    private final Map<String, WebSocketSession> ALL_SESSIONS = new HashMap<>();

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public MessageSender getMessageSender(){
        return new MessageSender(ALL_SESSIONS, EXECUTOR_SERVICE);
    }

    @Bean
    public SocketApplication getSocketApplication(
            @Autowired SocketControllersContext socketControllersNewContext,
            @Autowired BeanUtil beanUtil,
            @Autowired MessageSender messageSender){

        return new SocketApplication(socketControllersNewContext, applicationContext,
                beanUtil, EXECUTOR_SERVICE, ALL_SESSIONS, messageSender);
    }

    @Bean
    public SocketHandlerConfiguration getSocketHandlerConfiguration(){
        return new SocketHandlerConfiguration();
    }
}
