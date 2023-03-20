package com.jjerome.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class Response {
    private String resPath;
    private String resBody;
    private HttpStatus resStatus;

    public Response(String resPath, String resBody, HttpStatus resStatus){
        this.resBody = resBody;
        this.resPath = resPath;
        this.resStatus = resStatus;
    }

    public Response(String resBody, HttpStatus resStatus){
        this("/", resBody, resStatus);
    }

    Response(){
        this("/", "", HttpStatus.ACCEPTED);
    }

    public static Response jsonToResponse(String json) throws JsonProcessingException {
        return new ObjectMapper().readValue(json, Response.class);
    }

    public static String responseToJsonString(Response response) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(response);
    }
}
