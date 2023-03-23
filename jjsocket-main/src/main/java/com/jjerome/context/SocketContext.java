package com.jjerome.context;

import com.jjerome.annotations.SocketConnectMapping;
import com.jjerome.annotations.SocketDisconnectMapping;
import com.jjerome.annotations.SocketMapping;
import com.jjerome.exceptions.MappingParametersException;
import com.jjerome.exceptions.RequestPathBusy;
import com.jjerome.dto.Request;
import com.jjerome.dto.Response;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class SocketContext {

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
                        System.out.println(exception.getMessage() + " " + exception.getCause());
                    }
                });
                System.out.println("addConnectionMappings");
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException exception){
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
                        System.out.println(exception.getMessage() + " " + exception.getCause());
                    }
                });
                System.out.println("addDisconnectMappings");
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException exception){
                throw new RuntimeException("Socket controller constructor exception");
            }
        }
        return disconnectMappings;
    }

    public Map<String, Function<TextMessage, Response<?>>> addRequestsMappings(Class<?> controllerClass){
        Map<String, Function<TextMessage, Response<?>>> socketMappings = new HashMap<>();

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

                socketMappings.put(socketMapping.reqPath(), (message) -> {
                    try {
                        Request<?> request = new Request<>(message.getPayload(), reqGeneric);

                        if (request.getRequestBody() != null){
                            Response<?> response = (Response<?>) method.invoke(methodObject, request);

                            if (!Objects.equals(socketMapping.resPath(), "")){
                                response.setResPath(socketMapping.resPath());
                            }
                            return response;
                        }

                        return new Response<>("/error/json/request-body",
                                 "Request body does not match the requirements", HttpStatus.BAD_REQUEST);

                    } catch (InvocationTargetException | IllegalAccessException exception){
                        System.out.println(exception.getMessage() + " " + exception.getCause());

                        return new Response<>("/error/socket/mapping", "Socket mapping error",
                                HttpStatus.BAD_REQUEST);
                    }
                });

                System.out.println("addRequestsMappings");
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException exception){
                throw new RuntimeException("Socket controller constructor exception");
            }
        }
        return socketMappings;
    }
}
