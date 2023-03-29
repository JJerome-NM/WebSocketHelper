package com.jjerome.context;

import com.jjerome.annotations.SocketConnectMapping;
import com.jjerome.annotations.SocketDisconnectMapping;
import com.jjerome.annotations.SocketMapping;
import com.jjerome.annotations.SocketMappingFilter;
import com.jjerome.dto.Request;
import com.jjerome.dto.Response;
import com.jjerome.exceptions.MappingParametersException;
import com.jjerome.exceptions.RequestPathBusy;
import com.jjerome.models.RequestMapper;
import com.jjerome.models.ResponseErrors;
import com.jjerome.models.SocketMethodFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@Component
public class SocketControllersContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(SocketControllersContext.class);


    public Class<?> getMethodRequestGeneric(Method method){
        if (!(method.getGenericParameterTypes()[0] instanceof ParameterizedType parameterizedType)) return null;

        return (Class<?>) parameterizedType.getActualTypeArguments()[0];
    }

    public boolean validateMappingMethod(Method method, Class<?> returnClass, Class<?>... parameterClasses){
        if (method.getParameterCount() != parameterClasses.length) {
            throw new MappingParametersException("The number of parameters is not " + parameterClasses.length);
        }
        if (method.getReturnType() != returnClass) {
            throw new MappingParametersException("Return type is not " + returnClass.getName());
        }
        Class<?>[] methodParameters = method.getParameterTypes();
        for (int i = 0; i < parameterClasses.length; i++){
            if (methodParameters[i] != parameterClasses[i]) {
                throw new MappingParametersException("Bad parameters. Method - " + method.getName());
            }
        }
        return true;
    }

    public List<Consumer<WebSocketSession>> addConnectionMappings(Class<?> controllerClass) {
        List<Consumer<WebSocketSession>> connectionMappings = new ArrayList<>();

        for (Method method : controllerClass.getDeclaredMethods()){
            if (!method.isAnnotationPresent(SocketConnectMapping.class)) continue;
            if (!this.validateMappingMethod(method, void.class, WebSocketSession.class)) continue;

            try {
                Object methodObject = controllerClass.getDeclaredConstructor().newInstance();
                connectionMappings.add((webSocketSession) -> {
                    try {
                        method.invoke(methodObject, webSocketSession);
                    } catch (InvocationTargetException | IllegalAccessException exception){
                        LOGGER.error(exception.getMessage());
                    }
                });
            } catch (ReflectiveOperationException exception){
                throw new RuntimeException("Socket controller constructor exception");
            }
        }
        return connectionMappings;
    }

    public List<BiConsumer<WebSocketSession, CloseStatus>> addDisconnectMappings(Class<?> controllerClass){
        List<BiConsumer<WebSocketSession, CloseStatus>> disconnectMappings = new ArrayList<>();

        for (Method method : controllerClass.getDeclaredMethods()){
            if (!method.isAnnotationPresent(SocketDisconnectMapping.class)) continue;
            if (!this.validateMappingMethod(method, void.class, WebSocketSession.class, CloseStatus.class))  continue;

            try {
                Object methodObject = controllerClass.getDeclaredConstructor().newInstance();

                disconnectMappings.add((webSocketSession, closeStatus) -> {
                    try {
                        method.invoke(methodObject, webSocketSession, closeStatus);
                    } catch (InvocationTargetException | IllegalAccessException exception){
                        LOGGER.error(exception.getMessage());
                    }
                });
            } catch (ReflectiveOperationException exception){
                throw new RuntimeException("Socket controller constructor exception");
            }
        }
        return disconnectMappings;
    }

    public Map<String, BiFunction<WebSocketSession, TextMessage, Response<?>>> addRequestsMappings(
            Class<?> controllerClass){
        Map<String, BiFunction<WebSocketSession, TextMessage, Response<?>>> socketMappings = new HashMap<>();

        for (Method method : controllerClass.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(SocketMapping.class)) continue;
            if (!this.validateMappingMethod(method, Response.class, Request.class)) continue;

            SocketMapping socketMapping = method.getAnnotation(SocketMapping.class);

            if (socketMappings.containsKey(socketMapping.reqPath())) {
                throw new RequestPathBusy(controllerClass.getName() +
                        "; Method - " + method.getName() + " path is already in use");
            }

            try {
                Class<?> reqGeneric = this.getMethodRequestGeneric(method);
                Object methodObject = controllerClass.getDeclaredConstructor().newInstance();

                SocketMethodFilter methodFilter;
                if (method.isAnnotationPresent(SocketMappingFilter.class)){
                    methodFilter = method.getAnnotation(SocketMappingFilter.class).filter()
                            .getDeclaredConstructor().newInstance();
                } else methodFilter = (session, message, request) -> true;

                socketMappings.put(socketMapping.reqPath(), (session ,message) -> {
                    Request<?> request = RequestMapper.fromJSON(message.getPayload(), reqGeneric);

                    if (!methodFilter.doFilter(session, message, request)) return ResponseErrors.FILTERING_FAIL.get();
                    if (request.getRequestBody() == null) return ResponseErrors.REQUEST_BODY_NOT_REQ.get();

                    try {
                        Response<?> response = (Response<?>) method.invoke(methodObject, request);
                        if (!Objects.equals(socketMapping.resPath(), "")){
                            response.setResponsePath(socketMapping.resPath());
                        }
                        return response;
                    } catch (InvocationTargetException | IllegalAccessException exception){
                        LOGGER.error(exception.getMessage());
                        return ResponseErrors.MAPPING_ERROR.get();
                    }
                });
            } catch (ReflectiveOperationException exception){
                throw new RuntimeException("Socket controller constructor exception");
            }
        }
        return socketMappings;
    }
}
