package com.jjerome.test.MainTests.secret_tests;

import com.jjerome.annotations.FilteringOrder;
import com.jjerome.filters.SocketConnectionFilter;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@FilteringOrder(order = 3)
@Component
public class ThirdConnFileter implements SocketConnectionFilter {
    @Override
    public boolean doFilter( WebSocketSession session) {
        return true;
    }
}
