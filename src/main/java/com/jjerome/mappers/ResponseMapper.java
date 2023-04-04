package com.jjerome.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jjerome.dto.Response;
import com.jjerome.models.MessageSender;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageSender.class);

    public static  <RB> Response<RB> fromJSON(String json, Class<RB> reqBodyClass) {

        Response<RB> response = new Response<>();

        JSONObject jsonObject = new JSONObject(json);
        response.setResponsePath(jsonObject.getString("requestPath"));

        if (validateJsonField(jsonObject, "requestBody", reqBodyClass)){
            if (jsonFieldIsNumberOrString(jsonObject, "requestBody")){
                response.setResponseBody((RB) jsonObject.opt("requestBody"));
            } else {
                response.setResponseBody(JsonMapper.map(reqBodyClass, jsonObject.getJSONObject("requestBody")));
            }
        } else {
            response.setResponseBody(null);
        }
        return response;
    }

    public static <RB> String toJSON(Response<RB> response){
        try {
            return new ObjectMapper().writeValueAsString(response);
        } catch (JsonProcessingException exception){
            LOGGER.error(exception.getMessage());
        }
        return "";
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
