package com.jjerome.filters;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public interface SocketConnectionFilter {

    boolean doFilter(WebSocketSession session);
}
