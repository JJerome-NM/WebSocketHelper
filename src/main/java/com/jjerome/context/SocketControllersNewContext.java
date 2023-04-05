package com.jjerome.context;

import com.jjerome.annotations.SocketController;
import com.jjerome.annotations.SocketMapping;
import com.jjerome.annotations.SocketMappingFilters;
import com.jjerome.dto.Request;
import com.jjerome.exceptions.MappingParametersException;
import com.jjerome.exceptions.RequestPathBusy;
import com.jjerome.filters.FiltersComparator;
import com.jjerome.filters.SocketMessageFilter;
import com.jjerome.filters.SocketMethodFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;

@Component
public class SocketControllersNewContext {

//    private static final List<SocketMethodFilter>

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

    public Map<String, BiConsumer<WebSocketSession, TextMessage>> findRequestMappings(
            Class<SocketController> controllerClass){
        Map<String, BiConsumer<WebSocketSession, TextMessage>> socketMappings = new HashMap<>();

        for (Method method : controllerClass.getDeclaredMethods()){
            if (!method.isAnnotationPresent(SocketMapping.class)) continue;
            if (!this.validateMappingMethod(method, void.class, Request.class)) continue;

            String mappingPath = method.getAnnotation(SocketMapping.class).reqPath();

            if (socketMappings.containsKey(mappingPath)){
                throw new RequestPathBusy(
                        controllerClass.getName() + "; Method - " + method.getName() + " path is already in use");
            }

            Set<SocketMethodFilter> methodFilters = new TreeSet<>(new FiltersComparator<>());

            if (method.isAnnotationPresent(SocketMappingFilters.class)){
                for (Class<? extends SocketMethodFilter> filter :
                        method.getDeclaredAnnotation(SocketMappingFilters.class).filters()){


                }
            }
        }


        return socketMappings;
    }

}
