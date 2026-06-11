package com.example.inkstonetask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class InkstoneTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(InkstoneTaskApplication.class, args);
    }
}
