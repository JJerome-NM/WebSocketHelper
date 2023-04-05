package com.jjerome.test.MainTests.secret_tests;

import com.jjerome.annotations.*;
import com.jjerome.dto.Request;
import com.jjerome.models.MessageSender;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

@SocketController
@RequiredArgsConstructor
public class TestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestController.class);

    private final MessageSender messageSender;



    @SocketConnectMapping
    public void connectMap(WebSocketSession session){

        LOGGER.info(session.getId() + " - connected");
        messageSender.send(session.getId(), "/", "Hello user");
    }

    @SocketDisconnectMapping
    public void disconnect(WebSocketSession session, CloseStatus status){
        LOGGER.info(session.getId() + " - disconnected, with status - " + status);
    }

    @SocketMapping(reqPath = "/hello")
    public void hello(Request<String> request){

        messageSender.send(request.getSessionID(), "/hello", "Hello client");
    }

    @SocketMapping(reqPath = "/setYear")
    public void setYear(Request<Integer> request){

        messageSender.send(request.getSessionID(), "/hello", "Hello client");

//        return new Response<>("/", "Years - " + request.getRequestBody());
    }

    @SocketMapping(reqPath = "/getCar")
    @SocketMappingFilters(filters = {MappingFilter.class})
    public void getCar(Request<Car> request){
        System.out.println("Method car - " + request.getRequestBody());

        System.out.println(messageSender);

        messageSender.send(request.getSessionID(), "/newCar", new Car(2003, "BMW"));
    }
}

