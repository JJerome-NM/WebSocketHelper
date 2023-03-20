package com.jjerome.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

@Data
public class Request {

    private String reqPath;
    private String reqBody;

    public Request(String reqPath, String reqBody){
        this.reqBody = reqBody;
        this.reqPath = reqPath;
    }

    Request(String reqBody){
        this("/", reqBody);
    }

    Request(){
        this("/", "");
    }

    public static Request jsonToRequest(String json) throws JsonProcessingException {
        return new ObjectMapper().readValue(json, Request.class);
    }

    public static String requestToJsonString(Request request) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(request);
    }
}
