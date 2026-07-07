package com.ziro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ZiroApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZiroApplication.class, args);
    }
}
