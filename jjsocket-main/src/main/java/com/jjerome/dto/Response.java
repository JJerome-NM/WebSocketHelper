package com.jjerome.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response<RB> {
    private String responsePath;
    private RB responseBody;
    private int responseStatus;

    Response(RB responseBody, HttpStatus responseStatus){
        this.responsePath = "/";
        this.responseBody = responseBody;
        this.responseStatus = responseStatus.value();
    }

//    public Response(String resPath, RB resBody, HttpStatus resStatus){
//        this.resBody = resBody;
//        this.resPath = resPath;
//        this.resStatus = resStatus.value();
//    }
//
//    public Response(RB resBody, HttpStatus resStatus){
//        this("/", resBody, resStatus);
//    }

//    public Response<RB> fromJSON(String json){
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            JavaType requestType = objectMapper.getTypeFactory().constructParametricType(Response.class, Object.class);
//            Request<RB> newRequest = objectMapper.readValue(json, requestType);
//
//            this.resPath = newRequest.getRequestPath();
//            this.resBody = newRequest.getRequestBody();
//
//        } catch (JsonProcessingException exception){
//            System.out.println(exception.getMessage());
//        }
//        return this;
//    }
//
//    public String toJSON(){
//        try {
//            return new ObjectMapper().writeValueAsString(this);
//        } catch (JsonProcessingException exception){
//            System.out.println(exception.getMessage());
//        }
//        return null;
//    }
//
//    @Deprecated
//    public static Response<?> jsonToResponse(String json) throws JsonProcessingException {
//        return new ObjectMapper().readValue(json, Response.class);
//    }
//
//    @Deprecated
//    public static String responseToJsonString(Response<?> response) throws JsonProcessingException {
//        return new ObjectMapper().writeValueAsString(response);
//    }
}
