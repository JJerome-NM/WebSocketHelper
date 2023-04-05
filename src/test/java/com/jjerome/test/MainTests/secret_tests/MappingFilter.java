package com.jjerome.test.MainTests.secret_tests;

import com.jjerome.dto.Request;
import com.jjerome.filters.SocketMethodFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;


@Component
public class MappingFilter implements SocketMethodFilter {
    @Override
    public boolean doFilter(WebSocketSession session, TextMessage message, Request<?> request) {

        if (!(request.getRequestBody() instanceof Car car)) return false;

        return car.getName().equals("BMW");
    }
}
