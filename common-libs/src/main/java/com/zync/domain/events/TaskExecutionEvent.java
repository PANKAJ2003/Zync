package com.zync.domain.events;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskExecutionEvent {

    private UUID executionRunId;
    private UUID stepId;
    private String actionType;
    private JsonNode stepConfig;
    private JsonNode webhookPayload;
}