package com.jjerome.models;

import com.jjerome.dto.Response;

public enum ResponseErrors {
    FILTERING_FAIL("/error/filter", "Filtering failed", 406),
    MAPPING_NOT_FOUND("/error/mapping", "Mapping not found", 400),
    REQUEST_PATH_NULL("/error/json/request-path", "Request path is null", 400),
    REQUEST_BODY_NOT_REQ("/error/json/request-body",
            "Request body does not match the requirements", 400),
    MAPPING_ERROR("/error/socket/mapping", "Socket mapping error", 400),
    ;

    private final Response<String> response;

    ResponseErrors(String path, String message, int status) {
        this.response = new Response<>(path, message, status);
    }

    public Response<String> get(){
        return this.response;
    }
}
