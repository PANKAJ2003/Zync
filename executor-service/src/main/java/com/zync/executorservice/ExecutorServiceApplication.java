package com.zync.executorservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class ExecutorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExecutorServiceApplication.class, args);
    }

}
