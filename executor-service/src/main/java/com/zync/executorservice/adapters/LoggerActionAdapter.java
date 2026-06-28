package com.zync.executorservice.adapters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zync.domain.enums.StepStatus;
import com.zync.executorservice.core.AppActionAdapter;
import com.zync.executorservice.dto.TaskResultDTO;
import com.zync.executorservice.utils.TemplateResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adapter for the {@code LOG_MESSAGE} action type. Resolves message
 * templates against the webhook payload and writes them to the application
 * log for audit or debugging purposes.
 */
@Slf4j
@Component
public class LoggerActionAdapter implements AppActionAdapter {

    @Override
    public String getActionType() {
        return "LOG_MESSAGE";
    }

    /**
     * Resolves the message template and logs it. Always returns SUCCESS
     * since logging is a best-effort, non-critical operation.
     *
     * @param stepConfig     must contain a "message" key with optional {{$.}} placeholders
     * @param webhookPayload the payload used for placeholder resolution
     * @return SUCCESS with a confirmation message
     */
    @Override
    public TaskResultDTO execute(JsonNode stepConfig, JsonNode webhookPayload) {
        // Get the raw message template from the user's config
        String rawMessage = stepConfig.path("message").asText();

        // Resolve the dynamic variables!
        String finalMessage = TemplateResolver.resolve(rawMessage, webhookPayload);

        log.info("📢 [Zync Alert]: {}", finalMessage);

        ObjectNode resultNode = JsonNodeFactory.instance.objectNode();
        resultNode.put("message", "Logging successful");

        return TaskResultDTO.builder()
                .status(StepStatus.SUCCESS)
                .result(resultNode)
                .build();
    }
}