package com.jjerome.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jjerome.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Request<RB> {

    private String requestPath;

    private RB requestBody;


//    public Request<RB> fromJSON(String json, Class<RB> reqBodyClass) {
//
//        JSONObject jsonObject = new JSONObject(json);
//        this.requestPath = jsonObject.getString("requestPath");
//
//        if (this.validateJsonField(jsonObject, "requestBody", reqBodyClass)){
//            if (this.jsonFieldIsNumberOrString(jsonObject, "requestBody")){
//                this.requestBody = (RB) jsonObject.opt("requestBody");
//
//                return this;
//            }
//            this.requestBody = JsonMapper.map(reqBodyClass, jsonObject.getJSONObject("requestBody"));
//            return this;
//        }
//        this.requestBody = null;
//        return this;
//    }
//
//    private boolean jsonFieldIsNumberOrString(JSONObject jsonObject, String field){
//        Class<?> fieldClass = jsonObject.opt(field).getClass();
//
//        return fieldClass == String.class || fieldClass.getSuperclass() == Number.class;
//    }
//
//    private boolean validateJsonField(JSONObject jsonObject, String field, Class<RB> fieldClass){
//        if (jsonObject.has(field)){
//            Class<?> optFieldClass = jsonObject.opt(field).getClass();
//            return optFieldClass == JSONObject.class || optFieldClass == fieldClass
//                    || optFieldClass.getSuperclass() == Number.class
//                    && fieldClass.getSuperclass() == Number.class;
//        }
//        return false;
//    }
//
//    public String toJSON() throws JsonProcessingException{
//        return new ObjectMapper().writeValueAsString(this);
//    }
}
