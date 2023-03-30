package com.jjerome.test.MainTests;

import com.jjerome.annotations.FilteringOrder;
import com.jjerome.filters.SocketConnectionFilter;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.socket.WebSocketSession;

@FilteringOrder(order = 3)
public class ThirdConnFileter implements SocketConnectionFilter {
    @Override
    public boolean doFilter(@NotNull WebSocketSession session) {
        return true;
    }
}
