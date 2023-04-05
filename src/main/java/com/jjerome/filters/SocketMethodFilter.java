package com.jjerome.filters;

import com.jjerome.dto.Request;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
public interface SocketMethodFilter {

    boolean doFilter(WebSocketSession session, TextMessage message, Request<?> request);
}
