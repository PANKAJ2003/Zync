package com.zync.executorservice.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.zync.executorservice.dto.TaskResultDTO;

public interface AppActionAdapter {
    String getActionType();
    TaskResultDTO execute(JsonNode stepConfig, JsonNode webhookPayload);
}
