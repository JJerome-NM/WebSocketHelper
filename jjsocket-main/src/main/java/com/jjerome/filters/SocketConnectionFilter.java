package com.jjerome.filters;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.socket.WebSocketSession;

public interface SocketConnectionFilter {

    boolean doFilter(@NotNull WebSocketSession session);
}
