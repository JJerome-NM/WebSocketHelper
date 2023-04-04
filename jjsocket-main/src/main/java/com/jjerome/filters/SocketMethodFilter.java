package com.jjerome.filters;

import com.jjerome.dto.Request;
import lombok.NonNull;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public interface SocketMethodFilter {

    boolean doFilter(@NonNull WebSocketSession session, @NonNull TextMessage message, @NonNull Request<?> request);
}
