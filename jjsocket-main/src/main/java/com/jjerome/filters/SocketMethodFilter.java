package com.jjerome.filters;

import com.jjerome.dto.Request;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public interface SocketMethodFilter {

    boolean doFilter(@NotNull WebSocketSession session, @NotNull TextMessage message, @NotNull Request<?> request);
}
