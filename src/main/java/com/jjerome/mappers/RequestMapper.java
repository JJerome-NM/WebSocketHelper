package com.jjerome.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jjerome.dto.Request;

import org.json.JSONObject;

public class RequestMapper {


    public static String toJSON( Request<?> request) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(request);
    }

    public static  <RB> Request<RB> fromJSON(String json, Class<RB> reqBodyClass) {

        Request<RB> request = new Request<>();

        JSONObject jsonObject = new JSONObject(json);
        request.setRequestPath(jsonObject.getString("requestPath"));

        if (validateJsonField(jsonObject, "requestBody", reqBodyClass)){
            if (jsonFieldIsNumberOrString(jsonObject, "requestBody")){
                request.setRequestBody((RB) jsonObject.opt("requestBody"));
            } else {
                request.setRequestBody(JsonMapper.map(reqBodyClass, jsonObject.getJSONObject("requestBody")));
            }
        } else {
            request.setRequestBody(null);
        }
        return request;
    }

    private static boolean jsonFieldIsNumberOrString(JSONObject jsonObject, String field){
        Class<?> fieldClass = jsonObject.opt(field).getClass();

        return fieldClass == String.class || fieldClass.getSuperclass() == Number.class;
    }

    private static boolean validateJsonField(JSONObject jsonObject, String field, Class<?> fieldClass){
        if (jsonObject.has(field)){
            Class<?> optFieldClass = jsonObject.opt(field).getClass();
            return optFieldClass == JSONObject.class || optFieldClass == fieldClass
                    || optFieldClass.getSuperclass() == Number.class
                    && fieldClass.getSuperclass() == Number.class;
        }
        return false;
    }
}
