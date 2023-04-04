package com.jjerome.filters;

import org.springframework.web.socket.WebSocketSession;

public interface SocketConnectionFilter {

    boolean doFilter(WebSocketSession session);
}
