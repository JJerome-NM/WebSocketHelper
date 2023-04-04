package com.jjerome.filters;

import com.jjerome.dto.Request;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public interface SocketMethodFilter {

    boolean doFilter(WebSocketSession session, TextMessage message, Request<?> request);
}
