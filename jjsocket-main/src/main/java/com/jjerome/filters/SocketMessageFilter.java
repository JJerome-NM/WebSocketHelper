package com.jjerome.filters;

import lombok.NonNull;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public interface SocketMessageFilter {

    boolean doFilter(@NonNull WebSocketSession session, @NonNull TextMessage message);
}
