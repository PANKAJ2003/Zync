package com.zync.ingestion.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.zync.domain.events.WebhookEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

    private final KafkaTemplate<String, WebhookEvent> kafkaTemplate;

    public WebhookController(KafkaTemplate<String, WebhookEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    @PostMapping("/{webhookId}")
    public ResponseEntity<String> receiveWebhook(
            @PathVariable("webhookId") String webhookId,
            @RequestBody JsonNode rawPayload) {

        // Unique Trace ID for logging and tracking
        String traceId = UUID.randomUUID().toString();

        // Wrap the incoming data using our common-libs class
        WebhookEvent event = WebhookEvent.builder()
                .traceId(traceId)
                .webhookId(webhookId)
                .payload(rawPayload)
                .receivedAt(Instant.now())
                .build();

        kafkaTemplate.send("incoming-webhooks", webhookId, event);

        return ResponseEntity.accepted().body("Zync Event Accepted. Trace ID: " + traceId);
    }
}