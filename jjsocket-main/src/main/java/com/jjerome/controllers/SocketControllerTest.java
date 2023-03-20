package com.jjerome.controllers;

import com.jjerome.annotations.SocketController;
import com.jjerome.annotations.SocketMapping;
import com.jjerome.models.Request;
import com.jjerome.models.Response;
import org.springframework.http.HttpStatus;

@SocketController
public class SocketControllerTest {

    @SocketMapping(reqPath = "/SocketControllerTest/")
    public Response test(Request request){
        System.out.println("SocketControllerTest");

        return new Response(request.getReqPath(), request.getReqBody(), HttpStatus.ACCEPTED);
    }
}
