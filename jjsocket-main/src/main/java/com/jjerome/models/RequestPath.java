package com.jjerome.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jjerome.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.socket.TextMessage;

@Data
public class RequestPath {
    private String reqPath;

//    public RequestPath(String reqPath){
//        this.reqBody = reqBody;
//        this.reqPath = reqPath;
//    }

    RequestPath(TextMessage textMessage) {
        this(textMessage.getPayload());
    }

    RequestPath(String json){
        this.fromJSON(json);
    }

    RequestPath(){

    }

    public void fromJSON(String json) {
        RequestPath newRequest = JsonMapper.map(this.getClass(), json);

//            RequestPath newRequest = new ObjectMapper().readValue(json, RequestPath.class);

        this.reqPath = newRequest.getReqPath();
    }

    public String toJSON(){
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException exception){
            System.out.println(exception.getMessage());
        }
        return null;
    }
}
