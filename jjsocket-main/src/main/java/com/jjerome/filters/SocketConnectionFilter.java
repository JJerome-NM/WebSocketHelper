package com.jjerome.filters;

import lombok.NonNull;
import org.springframework.web.socket.WebSocketSession;

public interface SocketConnectionFilter {

    boolean doFilter(@NonNull WebSocketSession session);
}
