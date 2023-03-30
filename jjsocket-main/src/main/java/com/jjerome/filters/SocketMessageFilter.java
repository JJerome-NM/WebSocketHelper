package com.jjerome.filters;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public interface SocketMessageFilter {

    boolean doFilter(@NotNull WebSocketSession session, @NotNull TextMessage message);
}
