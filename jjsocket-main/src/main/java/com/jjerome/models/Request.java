package com.jjerome.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jjerome.JsonMapper;
import lombok.Data;
import lombok.Setter;
import lombok.SneakyThrows;
import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;

@Data
public class Request<RB> {

    private String reqPath;

    private RB reqBody;



    public Request(String reqPath, RB reqBody){
        this.reqBody = reqBody;
        this.reqPath = reqPath;
    }

    public Request (TextMessage textMessage, Class<RB> reqBodyClass) {
        this(textMessage.getPayload(), reqBodyClass);
    }

    public Request(String json, Class<RB> reqBodyClass){
        this.fromJSON(json, reqBodyClass);
    }


    private Request(){}


//    @Deprecated
//    public Request<RB> fromJSON(String json, Class<RB> reqBodyClass) throws JsonProcessingException {
//        ObjectMapper objectMapper = new ObjectMapper();
//        JavaType requestType = objectMapper.getTypeFactory().constructParametricType(Request.class, reqBodyClass);
//
//        Request<RB> newRequest = objectMapper.readValue(json, requestType);
//
//        this.reqPath = newRequest.getReqPath();
//        this.reqBody = newRequest.getReqBody();
//        return this;
//    }


    public Request<RB> fromJSON(String json, Class<RB> reqBodyClass) {

        JSONObject jsonObject = new JSONObject(json);
        this.reqPath = jsonObject.getString("reqPath");
        this.reqBody = JsonMapper.map(reqBodyClass, jsonObject.getJSONObject("reqBody"));

        return this;
    }

    public String toJSON() throws JsonProcessingException{
        return new ObjectMapper().writeValueAsString(this);
    }

    @Deprecated
    public static String requestToJsonString(Request<?> request) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(request);
    }
}
