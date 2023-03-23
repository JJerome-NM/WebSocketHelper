package com.jjerome.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class Response<RB> {
    private String resPath;
    private RB resBody;
    private int resStatus;

    public Response(String resPath, RB resBody, HttpStatus resStatus){
        this.resBody = resBody;
        this.resPath = resPath;
        this.resStatus = resStatus.value();
    }

    public Response(RB resBody, HttpStatus resStatus){
        this("/", resBody, resStatus);
    }

    Response(){}

    public Response<RB> fromJSON(String json){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JavaType requestType = objectMapper.getTypeFactory().constructParametricType(Request.class, Object.class);
            Request<RB> newRequest = objectMapper.readValue(json, requestType);

            this.resPath = newRequest.getRequestPath();
            this.resBody = newRequest.getRequestBody();

        } catch (JsonProcessingException exception){
            System.out.println(exception.getMessage());
        }
        return this;
    }

    public String toJSON(){
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException exception){
            System.out.println(exception.getMessage());
        }
        return null;
    }

    @Deprecated
    public static Response<?> jsonToResponse(String json) throws JsonProcessingException {
        return new ObjectMapper().readValue(json, Response.class);
    }

    @Deprecated
    public static String responseToJsonString(Response<?> response) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(response);
    }
}
