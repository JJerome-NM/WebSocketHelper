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
}
