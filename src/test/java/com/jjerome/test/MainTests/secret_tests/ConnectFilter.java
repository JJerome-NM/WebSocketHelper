package com.jjerome.test.MainTests.secret_tests;

import com.jjerome.annotations.FilteringOrder;
import com.jjerome.filters.SocketConnectionFilter;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@FilteringOrder(order = 1)
@Component
public class ConnectFilter implements SocketConnectionFilter {
    @Override
    public boolean doFilter( WebSocketSession session) {
        return true;
    }
}
