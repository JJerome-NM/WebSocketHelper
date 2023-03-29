package com.jjerome.test.MainTests.test;

import com.jjerome.annotations.SocketComponentsScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@SocketComponentsScan(packages = {"com.jjerome.test"})
public class MainTests {
    public static void main(String[] args) {
        SpringApplication.run(MainTests.class, args);
    }
}
