package com.jjerome.test.MainTests;


import com.jjerome.models.SocketMessageFilter;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class MessageFilter implements SocketMessageFilter {

    @Override
    public boolean doFilter(@NotNull WebSocketSession session, @NotNull TextMessage message) {
        System.out.println(message);

        return true;
    }
}
