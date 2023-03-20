package com.jjerome.models;

import org.springframework.http.HttpStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketHelper {
    private final Socket socket;
    private final PrintWriter writer;
    private final BufferedReader reader;


    public SocketHelper(Socket socket) throws IOException {
        this.socket = socket;
        this.writer = new PrintWriter(this.socket.getOutputStream(), true);
        this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }

    public Request getRequest() throws IOException {
        return Request.jsonToRequest(this.reader.readLine());
    }

    public void sendResponse(String resPath, String resBody, HttpStatus status) throws IOException {
        Response response = new Response(resPath, resBody, status);
        this.writer.println(Response.responseToJsonString(response));
    }

    public void sendResponse(Response response) throws IOException {
        this.writer.println(Response.responseToJsonString(response));
    }
}
