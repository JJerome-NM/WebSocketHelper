package com.jjerome.models;

import com.jjerome.filters.SocketMessageFilter;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;

@RequiredArgsConstructor
public class RequestAccepter {

    private final MessageSender messageSender;

    private final Map<String, BiConsumer<WebSocketSession ,TextMessage>> methodMappings;

    private final Set<SocketMessageFilter> messageFilters;

    private final ExecutorService executorService;

    public void acceptMessage(@NotNull WebSocketSession session, @NotNull TextMessage message){
        this.executorService.submit(() -> {
            for (SocketMessageFilter filter : this.messageFilters){
                if (!filter.doFilter(session, message)){
                    messageSender.send(session.getId(), ResponseErrors.FILTERING_FAIL.getResponse());
                    return;
                }
            }

            if (new JSONObject(message.getPayload()).has("requestPath")){
                String requestPath = new JSONObject(message.getPayload()).getString("requestPath");

                if (this.methodMappings.containsKey(requestPath)){
                    this.methodMappings.get(requestPath).accept(session, message);
                } else {
                    messageSender.send(session.getId(), ResponseErrors.MAPPING_NOT_FOUND.getResponse());
                }
            } else {
                messageSender.send(session.getId(), ResponseErrors.REQUEST_PATH_NULL.getResponse());
            }
        });
    }
}
