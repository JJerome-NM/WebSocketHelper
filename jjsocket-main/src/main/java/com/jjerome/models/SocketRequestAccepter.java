package com.jjerome.models;

import com.jjerome.dto.Response;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;

public class SocketRequestAccepter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketRequestAccepter.class);

    private final Map<String, BiFunction<WebSocketSession ,TextMessage, Response<?>>> methodMappings;

    private final List<SocketMessageFilter> messageFilters;

    private final ExecutorService executorService;

    public SocketRequestAccepter(Map<String, BiFunction<WebSocketSession ,TextMessage, Response<?>>> methodMappings,
                                  List<SocketMessageFilter> messageFilters,
                                  ExecutorService executorService) {
        this.methodMappings = methodMappings;
        this.messageFilters = messageFilters;
        this.executorService = executorService;
    }

    public void acceptMessage(@NotNull WebSocketSession session, @NotNull TextMessage message){
        this.executorService.submit(() -> {
            for (SocketMessageFilter filter : this.messageFilters){
                if (!filter.doFilter(session, message)){
                    try {
                        session.sendMessage(new TextMessage(ResponseMapper.toJSON(
                                ResponseErrors.FILTERING_FAIL.get())));
                        return;
                    } catch (IOException exception){
                        LOGGER.error(exception.getMessage());
                        return;
                    }
                }
            }

            if (new JSONObject(message.getPayload()).has("requestPath")){
                this.executorService.submit(() -> {
                    try{
                        String requestPath = new JSONObject(message.getPayload()).getString("requestPath");
                        Response<?> response;

                        if (this.methodMappings.containsKey(requestPath)){
                            response = this.methodMappings.get(requestPath).apply(session, message);
                        } else response = ResponseErrors.MAPPING_NOT_FOUND.get();

                        session.sendMessage(new TextMessage(ResponseMapper.toJSON(response)));
                    } catch (IOException exception){
                        LOGGER.error(exception.getMessage());
                    }
                });
            } else {
                try {
                    session.sendMessage(new TextMessage(ResponseMapper.toJSON(
                            ResponseErrors.REQUEST_PATH_NULL.get())));
                } catch (IOException exception){
                    LOGGER.error(exception.getMessage());
                }
            }
        });
    }
}
