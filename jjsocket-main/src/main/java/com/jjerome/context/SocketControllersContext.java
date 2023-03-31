package com.jjerome.context;

import com.jjerome.annotations.*;
import com.jjerome.dto.Request;
import com.jjerome.exceptions.ExceptionMessage;
import com.jjerome.exceptions.MappingParametersException;
import com.jjerome.exceptions.RequestPathBusy;
import com.jjerome.filters.FiltersComparator;
import com.jjerome.mappers.RequestMapper;
import com.jjerome.models.MessageSender;
import com.jjerome.models.ResponseErrors;
import com.jjerome.filters.SocketMethodFilter;
import com.jjerome.models.SocketApplication;
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
import java.util.function.Consumer;

@Component
public class SocketControllersContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(SocketControllersContext.class);

    private final MessageSender messageSender = SocketApplication.getMessageSender();


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
            if (!this.validateMappingMethod(method, void.class, WebSocketSession.class, CloseStatus.class)) continue;

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

    public Map<String, BiConsumer<WebSocketSession, TextMessage>> addRequestsMappings(
            Class<?> controllerClass){
        Map<String, BiConsumer<WebSocketSession, TextMessage>> socketMappings = new HashMap<>();

        for (Method method : controllerClass.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(SocketMapping.class)) continue;
            if (!this.validateMappingMethod(method, void.class, Request.class)) continue;

            SocketMapping socketMapping = method.getAnnotation(SocketMapping.class);

            if (socketMappings.containsKey(socketMapping.reqPath())) {
                throw new RequestPathBusy(controllerClass.getName() +
                        "; Method - " + method.getName() + " path is already in use");
            }

            try {
                Set<SocketMethodFilter> methodFilters = new TreeSet<>(new FiltersComparator<>());

                if (method.isAnnotationPresent(SocketMappingFilters.class)){
                    for (Class<? extends  SocketMethodFilter> filter :
                            method.getDeclaredAnnotation(SocketMappingFilters.class).filters()){

                        if (!filter.isAnnotationPresent(FilteringOrder.class)){
                            LOGGER.warn(filter.getName() + " " + ExceptionMessage.CLASS_DONT_HAVE_FILTERING_ORDER.get());
                        }

                        methodFilters.add(filter.getDeclaredConstructor().newInstance());
                    }
                }

                Class<?> reqGeneric = this.getMethodRequestGeneric(method);
                Object methodObject = controllerClass.getDeclaredConstructor().newInstance();

                socketMappings.put(socketMapping.reqPath(), (session ,message) -> {
                    Request<?> request = RequestMapper.fromJSON(message.getPayload(), reqGeneric);
                    request.setSessionID(session.getId());

                    for (SocketMethodFilter filter : methodFilters){
                        if (!filter.doFilter(session, message, request)){
                            messageSender.send(session.getId(), ResponseErrors.FILTERING_FAIL.get());
                            return;
                        }
                    }

                    if (request.getRequestBody() == null){
                        messageSender.send(session.getId(), ResponseErrors.REQUEST_BODY_NOT_REQ.get());
                        return;
                    }

                    try {
                        method.invoke(methodObject, request);
                    } catch (InvocationTargetException | IllegalAccessException exception){
                        LOGGER.error("Method invoke exception", exception);
                    }
                });
            } catch (ReflectiveOperationException exception){
                throw new RuntimeException("Socket controller constructor exception");
            }
        }
        return socketMappings;
    }
}
