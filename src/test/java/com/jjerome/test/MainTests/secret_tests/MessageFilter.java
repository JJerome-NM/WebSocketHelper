package com.jjerome.test.MainTests.secret_tests;


import com.jjerome.filters.SocketMessageFilter;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
public class MessageFilter implements SocketMessageFilter {

    @Override
    public boolean doFilter( WebSocketSession session,  TextMessage message) {
        return true;
    }
}
