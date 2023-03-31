package com.jjerome.models;

import com.jjerome.filters.SocketMessageFilter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;

public class RequestAccepter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestAccepter.class);

    private final MessageSender MESSAGE_SENDER = SocketApplication.getMessageSender();

    private final Map<String, BiConsumer<WebSocketSession ,TextMessage>> methodMappings;

    private final Set<SocketMessageFilter> messageFilters;

    private final ExecutorService executorService;

    public RequestAccepter(Map<String, BiConsumer<WebSocketSession ,TextMessage>> methodMappings,
                           Set<SocketMessageFilter> messageFilters,
                           ExecutorService executorService) {
        this.methodMappings = methodMappings;
        this.messageFilters = messageFilters;
        this.executorService = executorService;
    }

    public void acceptMessage(@NotNull WebSocketSession session, @NotNull TextMessage message){
        this.executorService.submit(() -> {
            for (SocketMessageFilter filter : this.messageFilters){
                if (!filter.doFilter(session, message)){
                    MESSAGE_SENDER.send(session.getId(), ResponseErrors.FILTERING_FAIL.get());
                }
            }

            if (new JSONObject(message.getPayload()).has("requestPath")){
                String requestPath = new JSONObject(message.getPayload()).getString("requestPath");

                if (this.methodMappings.containsKey(requestPath)){
                    this.methodMappings.get(requestPath).accept(session, message);
                } else {
                    MESSAGE_SENDER.send(session.getId(), ResponseErrors.MAPPING_NOT_FOUND.get());
                }
            } else {
                MESSAGE_SENDER.send(session.getId(), ResponseErrors.REQUEST_PATH_NULL.get());
            }
        });
    }
}
