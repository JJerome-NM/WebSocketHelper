package com.jjerome.controllers;

import com.jjerome.models.Request;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class MainController {

    public static void main(String[] args) {
        SpringApplication.run(MainController.class, args);


//        String req = "{\n" +
//                "    \"reqPath\": \"/getCar\",\n" +
//                "    \"reqBody\": {\n" +
//                "        \"name\": \"Posrh\",\n" +
//                "        \"year\": 2021\n" +
//                "    }\n" +
//                "}";
//
//
//        Request<Bus> carRequest = new Request<>(req, Bus.class);
//
//        carRequest.fromJSON(req, Bus.class);
//        System.out.println(carRequest);
//        carRequest.fromJSON(req, Bus.class);
//        System.out.println(carRequest);
//        carRequest.fromJSON(req, Bus.class);
//        System.out.println(carRequest);
//        carRequest.fromJSON(req, Bus.class);
//        System.out.println(carRequest);
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Bus {
    private int year;
    private String name;
}
