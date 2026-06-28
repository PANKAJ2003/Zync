package com.zync.executorservice.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.zync.executorservice.dto.TaskResultDTO;

/**
 * Strategy contract for executing a workflow action type. Each implementation
 * handles a specific action (e.g., HTTP_REQUEST, AI_PROMPT) and is discovered
 * by {@link ActionRouter} via the {@link #getActionType()} key.
 */
public interface AppActionAdapter {
    String getActionType();

    TaskResultDTO execute(JsonNode stepConfig, JsonNode webhookPayload);
}
