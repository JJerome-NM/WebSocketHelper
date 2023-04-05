package com.jjerome.test.MainTests.secret_tests.test;

import com.jjerome.annotations.EnableSockets;
import com.jjerome.annotations.SocketComponentsScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@SocketComponentsScan(packages = {"com.jjerome.test"})
@ComponentScan(basePackages = {"com.jjerome.test", "com.jjerome.models"})
@EnableSockets
public class MainTests {
    public static void main(String[] args) {
        SpringApplication.run(MainTests.class, args);
    }
}
