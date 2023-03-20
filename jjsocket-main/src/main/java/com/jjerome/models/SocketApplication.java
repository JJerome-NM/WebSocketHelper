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

    private final int port;

    private final List<SocketRequestAccepter> socketThreads = new ArrayList<>();

    private final Map<String, Function<Request, Response>> socketMappings = new HashMap<>();

    public SocketApplication(int port) {
        try{
            this.port = port;
            this.serverSocket = new ServerSocket(this.port);
        } catch (IOException exception){
            throw new RuntimeException("Server socket error " + exception.getMessage());
        }
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
        try {
            ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            service.submit(new SocketRequestAccepter(socket, this.socketMappings));

//            this.socketThreads.add(new SocketRequestAccepter(socket, this.socketMappings));
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }

    public void run(String packagePath){
        Reflections reflections = new Reflections(packagePath);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(SocketController.class);

        classes.forEach(this::addSocketController);
        this.startAcceptSockets();
    }
    public void addSocketController(Class<?> controllerClass){
        if (controllerClass.isAnnotationPresent(SocketController.class)){
            for (Method method : controllerClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(SocketMapping.class)){
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
            }
        } else {
            throw new RuntimeException(controllerClass + " this class is not a Socket Controller");
        }
        System.out.println(controllerClass.getName() + " class added");
    }
}
