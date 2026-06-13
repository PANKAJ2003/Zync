package com.zync.domain.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookEvent {
    private String traceId;       // To track the request
    private String webhookId;     // The unique ID from the URL (e.g., /webhook/{id})
    private JsonNode payload;     // The raw, unknown JSON payload
    private Instant receivedAt;
}