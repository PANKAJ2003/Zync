package com.zync.executorservice.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TaskConsumer {

    @KafkaListener(topics = "task-execution", groupId = "executor-service")
    public void consume(String message) {
        System.out.println("Received message: " + message);
    }
}
