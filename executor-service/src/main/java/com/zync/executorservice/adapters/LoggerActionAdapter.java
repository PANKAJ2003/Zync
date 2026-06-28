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

@Slf4j
@Component
public class LoggerActionAdapter implements AppActionAdapter {

    @Override
    public String getActionType() {
        return "LOG_MESSAGE";
    }

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