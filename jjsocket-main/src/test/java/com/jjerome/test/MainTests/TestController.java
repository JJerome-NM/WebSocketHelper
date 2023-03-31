package com.jjerome.test.MainTests;

import com.jjerome.annotations.*;
import com.jjerome.dto.Request;
import com.jjerome.models.MessageSender;
import com.jjerome.models.SocketApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

@SocketController
public class TestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestController.class);

    private static final MessageSender MESSAGE_SENDER = SocketApplication.getMessageSender();

    @SocketConnectMapping
    public void connectMap(WebSocketSession session){

        LOGGER.info(session.getId() + " - connected");
        MESSAGE_SENDER.send(session.getId(), "/", "Hello user");
    }

    @SocketDisconnectMapping
    public void disconnect(WebSocketSession session, CloseStatus status){
        LOGGER.info(session.getId() + " - disconnected, with status - " + status);
    }

    @SocketMapping(reqPath = "/hello")
    public void hello(Request<String> request){

        MESSAGE_SENDER.send(request.getSessionID(), "/hello", "Hello client");
    }

    @SocketMapping(reqPath = "/setYear")
    public void setYear(Request<Integer> request){

        MESSAGE_SENDER.send(request.getSessionID(), "/hello", "Hello client");

//        return new Response<>("/", "Years - " + request.getRequestBody());
    }

    @SocketMapping(reqPath = "/getCar")
    @SocketMappingFilters(filters = {MappingFilter.class})
    public void getCar(Request<Car> request){
        System.out.println("Method car - " + request.getRequestBody());

        MESSAGE_SENDER.send(request.getSessionID(), "/newCar", new Car(2003, "BMW"));
    }
}

