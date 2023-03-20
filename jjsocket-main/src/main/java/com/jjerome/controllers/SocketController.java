package com.jjerome.controllers;

import com.jjerome.annotations.SocketMapping;
import com.jjerome.models.Request;
import com.jjerome.models.Response;
import jakarta.servlet.annotation.WebFilter;
import org.springframework.http.HttpStatus;

@com.jjerome.annotations.SocketController
public class SocketController {


    @SocketMapping(reqPath = "/test", resPath = "/test")
    public Response test(Request request){
        System.out.println("WOOOOOOOOOOOOOOOOW");
        return new Response("/all/good/", request.getReqBody(), HttpStatus.ACCEPTED);
    }

    @SocketMapping(reqPath = "/test2")
    public Response test2(Request request){
        System.out.println("WOOOOW22");
        return new Response("/all/good/2", request.getReqBody(), HttpStatus.ACCEPTED);
    }

    @SocketMapping(reqPath = "/threads")
    public Response threads(Request request){
        System.out.println("Threads");
        return new Response("/all/good/2", request.getReqBody(), HttpStatus.ACCEPTED);
    }
}
