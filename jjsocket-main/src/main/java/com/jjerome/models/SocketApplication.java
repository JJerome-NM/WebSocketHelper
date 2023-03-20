package com.jjerome.models;

import com.jjerome.annotations.SocketController;
import com.jjerome.annotations.SocketMapping;
import org.reflections.Reflections;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class SocketApplication {
    private final ServerSocket serverSocket;

    private final Map<String, Function<Request<?>, Response>> socketMappings = new HashMap<>();

    private final ExecutorService executorService = Executors.newFixedThreadPool(100);
//    private final ExecutorService executorService = Executors.newFixedThreadPool(
//            Runtime.getRuntime().availableProcessors());

    public SocketApplication(int port) {
        try{
            this.serverSocket = new ServerSocket(port);
        } catch (IOException exception){
            throw new RuntimeException("Server socket error " + exception.getMessage());
        }
    }

    public SocketApplication(){
        this(9090);
    }

    public void startAcceptSockets(){
        try{
            while(true){
                this.addSocketAcceptThread(this.serverSocket.accept());
            }
        } catch (IOException exception){
            throw new RuntimeException("Socket accept exception");
        }
    }

    private void addSocketAcceptThread(Socket socket){
        try{
            this.executorService.submit(new SocketRequestAccepter(socket, this.socketMappings));
        } catch (IOException exception){
            System.out.println(exception.getMessage());
        }
    }

    public void run(String packageName){
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(SocketController.class);

        classes.forEach(this::addSocketController);
        this.startAcceptSockets();
    }
    private void addSocketController(Class<?> controllerClass){
        if (!controllerClass.isAnnotationPresent(SocketController.class)){
            throw new RuntimeException(controllerClass + " this class is not a Socket Controller");
        }
        for (Method method : controllerClass.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(SocketMapping.class)) return;
            if (method.getParameterCount() != 1) return;
            if (method.getParameterTypes()[0] != Request.class) return;
            if (method.getReturnType() != Response.class) return;

            SocketMapping socketMapping = method.getAnnotation(SocketMapping.class);

            try {
                Object methodObject = controllerClass.getDeclaredConstructor().newInstance();

                if (socketMappings.containsKey(socketMapping.reqPath())){
                    throw new RuntimeException(controllerClass.getName() +
                            "; Method - " + method.getName() + " path is already in use");
                }

                socketMappings.put(socketMapping.reqPath(), (request) -> {
                    try {
                        Response response = (Response) method.invoke(methodObject, request);
                        if (!Objects.equals(socketMapping.resPath(), "")){
                            response.setResPath(socketMapping.resPath());
                        }
                        return response;

                    } catch (InvocationTargetException | IllegalAccessException exception){
                        return new Response("/error", "Socket mapping error",
                                HttpStatus.BAD_REQUEST);
                    }
                });
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException exception){
                throw new RuntimeException("Socket controller constructor exception");
            }
        }
        System.out.println(controllerClass.getName() + " class added");
    }
}
