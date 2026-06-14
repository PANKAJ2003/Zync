package com.zync.orchestratorservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.JsonNode;

public record StepRequestDTO(

        @NotNull(message = "Step order is required")
        @Min(value = 1, message = "Step order must be 1 or greater")
        Integer stepOrder,

        @NotNull(message = "Action type (e.g., SLACK_MESSAGE) is required")
        String actionType,

        @NotNull(message = "Configuration is required")
        JsonNode config) {
}
