package com.jjerome.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Request<RB> {

    private String sessionID;

    private String requestPath;

    private RB requestBody;
}
