package com.jjerome.controllers;

import com.jjerome.annotations.SocketConnectMapping;
import com.jjerome.annotations.SocketController;
import com.jjerome.annotations.SocketDisconnectMapping;
import com.jjerome.annotations.SocketMapping;
import com.jjerome.dto.Request;
import com.jjerome.dto.Response;
import org.springframework.http.HttpStatus;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@SocketController
public class TestController {

    @SocketConnectMapping
    public void connectmap(WebSocketSession session){
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
        return new Response<>("Hello " + request.getRequestBody(), HttpStatus.ACCEPTED);
    }

    @SocketMapping(reqPath = "/setYear")
    public Response<String> setYear(Request<Integer> request){
        return new Response<>("Years - " + request.getRequestBody(), HttpStatus.ACCEPTED);
    }

    @SocketMapping(reqPath = "/getCar")
    public Response<Car> getCar(Request<Car> request){
        Car car = request.getRequestBody();

        System.out.println("Method car - " + car);


        System.out.println(request.getRequestBody().getClass());

        return new Response<>(new Car(2003, "BMW"), HttpStatus.ACCEPTED);
    }
}

