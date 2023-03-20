package com.jjerome.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

@Data
public class Request<RB> {

    private String reqPath;
    private RB reqBody;

    public Request(String reqPath, RB reqBody){
        this.reqBody = reqBody;
        this.reqPath = reqPath;
    }

    Request(RB reqBody){
        this("/", reqBody);
    }

    Request(){}

    public static Request<?> jsonToRequest(String json) throws JsonProcessingException {
        return new ObjectMapper().readValue(json, Request.class);
    }

    public static String requestToJsonString(Request<?> request) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(request);
    }
}
