package com.jjerome.filters;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public interface SocketMessageFilter {

    boolean doFilter(@NotNull WebSocketSession session, @NotNull TextMessage message);
}
