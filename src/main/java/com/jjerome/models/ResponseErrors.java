package com.jjerome.models;

import com.jjerome.dto.Response;

public enum ResponseErrors {
    FILTERING_FAIL("/error/filter", "Filtering failed"),
    MAPPING_NOT_FOUND("/error/mapping", "Mapping not found"),
    REQUEST_PATH_NULL("/error/json/request-path", "Request path is null"),
    REQUEST_BODY_NOT_REQ("/error/json/request-body",
            "Request body does not match the requirements"),
    MAPPING_ERROR("/error/socket/mapping", "Socket mapping error"),
    ;

    private final Response<String> response;

    ResponseErrors(String path, String message) {
        this.response = new Response<>(path, message);
    }

    public Response<String> get(){
        return this.response;
    }
}
