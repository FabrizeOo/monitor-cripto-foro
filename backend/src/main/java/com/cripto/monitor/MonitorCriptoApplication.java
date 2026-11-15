package com.cripto.monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MonitorCriptoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonitorCriptoApplication.class, args);
    }
}

