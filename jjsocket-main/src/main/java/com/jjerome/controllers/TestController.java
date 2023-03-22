package com.jjerome.controllers;

import com.jjerome.annotations.SocketConnectMapping;
import com.jjerome.annotations.SocketController;
import com.jjerome.annotations.SocketDisconnectMapping;
import com.jjerome.annotations.SocketMapping;
import com.jjerome.models.Request;
import com.jjerome.models.Response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@SocketController
public class TestController {

    @SocketMapping(reqPath = "/hello")
    public Response<String> hello(Request<String> request){
        return new Response<>("Hello client", HttpStatus.ACCEPTED);
    }

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
        System.out.println(session);
        System.out.println(status);
    }

    @SocketMapping(reqPath = "/getCar")
    public Response<Car> getCar(Request<Car> request){
        Car car = request.getReqBody();

        System.out.println("Method car - " + car);


        System.out.println(request.getReqBody().getClass());

        return new Response<>(new Car(2003, "BMW"), HttpStatus.ACCEPTED);
    }
}

