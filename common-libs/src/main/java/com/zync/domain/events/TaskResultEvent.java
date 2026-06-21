package com.zync.domain.events;

import com.fasterxml.jackson.databind.JsonNode;
import com.zync.domain.enums.StepStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResultEvent {
    private UUID executionRunId;
    private UUID stepId;
    private String actionType;
    private StepStatus status;
    private String errorMessage;
    private JsonNode result;     // (Optional) If the step returns data, like a new Stripe ID
}