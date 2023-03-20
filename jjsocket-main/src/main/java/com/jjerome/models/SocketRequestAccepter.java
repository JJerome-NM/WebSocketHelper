package com.jjerome.models;

import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.function.Function;

public class SocketRequestAccepter implements Runnable {
    private final SocketHelper socketHelper;
    private boolean end = false;
    private final Map<String, Function<Request, Response>> socketMappings;

    SocketRequestAccepter(SocketHelper socketHelper,
                          Map<String, Function<Request, Response>> socketMappings){
        this.socketHelper = socketHelper;
        this.socketMappings = socketMappings;
    }

    SocketRequestAccepter(Socket socket,
                          Map<String, Function<Request, Response>> socketMappings) throws IOException {
        this(new SocketHelper(socket), socketMappings);
    }

    @Override
    public void run() {
        try{
            while (true) {
                Request request = this.socketHelper.getRequest();

//                System.out.println(request);

                if (this.socketMappings.containsKey(request.getReqPath())){
                    Response response = this.socketMappings.get(request.getReqPath()).apply(request);
                    this.socketHelper.sendResponse(response);
                } else {
                    this.socketHelper.sendResponse("/error",
                            request.getReqPath() + " method url not found", HttpStatus.BAD_REQUEST);
                }
            }
        } catch (IOException exception){
            System.out.println(exception.getMessage());
        }
    }
}
