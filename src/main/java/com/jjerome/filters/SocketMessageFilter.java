package com.jjerome.filters;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public interface SocketMessageFilter {

    boolean doFilter(WebSocketSession session, TextMessage message);
}
