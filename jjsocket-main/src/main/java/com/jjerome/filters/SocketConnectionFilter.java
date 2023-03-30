package com.jjerome.filters;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.socket.WebSocketSession;

public interface SocketConnectionFilter {

    boolean doFilter(@NotNull WebSocketSession session);
}
