package com.jjerome.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response<RB> {
    private String responsePath;
    private RB responseBody;
    Response(RB responseBody){
        this.responsePath = "/";
        this.responseBody = responseBody;
    }
}
