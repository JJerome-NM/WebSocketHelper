package com.jjerome.filters;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
public interface SocketMessageFilter {

    boolean doFilter(WebSocketSession session, TextMessage message);
}
