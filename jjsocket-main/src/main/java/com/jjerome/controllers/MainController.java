package com.jjerome.controllers;

import com.jjerome.models.SocketApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class MainController {
    public static void main(String[] args) {
        SocketApplication socketApplication = new SocketApplication(8001);
        socketApplication.run("com.jjerome");
    }
}
