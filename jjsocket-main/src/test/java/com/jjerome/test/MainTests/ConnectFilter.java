package com.jjerome.test.MainTests;

import com.jjerome.models.SocketConnectionFilter;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.socket.WebSocketSession;

public class ConnectFilter implements SocketConnectionFilter {
    @Override
    public boolean doFilter(@NotNull WebSocketSession session) {
        return true;
    }
}
