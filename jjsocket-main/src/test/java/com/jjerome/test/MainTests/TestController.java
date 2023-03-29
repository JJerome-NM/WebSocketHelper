package com.jjerome.test.MainTests;

import com.jjerome.annotations.*;
import com.jjerome.dto.Request;
import com.jjerome.dto.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@SocketController
public class TestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestController.class);


    @SocketConnectMapping
    public void connectMap(WebSocketSession session){

        LOGGER.info(session.getId() + " - connected");
        try{
            session.sendMessage(new TextMessage("Hello user"));
        } catch (IOException exception){
            System.out.println(exception.getMessage());
        }

    }

    @SocketDisconnectMapping
    public void disconnect(WebSocketSession session, CloseStatus status){
        System.out.println(session.getId() + " - disconnected");
    }

    @SocketMapping(reqPath = "/hello")
    public Response<String> hello(Request<String> request){
        return new Response<>("/", "Hello " + request.getRequestBody(), 400);
    }

    @SocketMapping(reqPath = "/setYear")
    public Response<String> setYear(Request<Integer> request){
        return new Response<>("/", "Years - " + request.getRequestBody(), 400);
    }

    @SocketMapping(reqPath = "/getCar")
    @SocketMappingFilter(filter = MappingFilter.class)
    public Response<Car> getCar(Request<Car> request){
        Car car = request.getRequestBody();

        System.out.println("Method car - " + car);


        System.out.println(request.getRequestBody().getClass());

        return new Response<>("/", new Car(2003, "BMW"), 400);
    }
}

