package com.jjerome.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDto {
    private String resPath;
    private String resBody;
    private HttpStatus resStatus;
}
