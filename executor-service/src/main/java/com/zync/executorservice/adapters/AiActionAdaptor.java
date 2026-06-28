package com.zync.executorservice.adapters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zync.domain.enums.StepStatus;
import com.zync.executorservice.core.AppActionAdapter;
import com.zync.executorservice.dto.TaskResultDTO;
import com.zync.executorservice.utils.TemplateResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AiActionAdaptor implements AppActionAdapter {

    private final ChatClient chatClient;

    public AiActionAdaptor(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public String getActionType() {
        return "AI_PROMPT";
    }

    @Override
    public TaskResultDTO execute(JsonNode stepConfig, JsonNode webhookPayload) {

        String rawPrompt = stepConfig.path("prompt").asText();
        String resolvedPrompt = TemplateResolver.resolve(rawPrompt, webhookPayload);

        log.info("Sending prompt to AI: {}", resolvedPrompt);

        try {
            String aiResponse = chatClient.prompt()
                    .user(resolvedPrompt)
                    .call()
                    .content();

            log.info("AI Response: {}", aiResponse);

            ObjectNode resultNode = JsonNodeFactory.instance.objectNode();
            resultNode.put("ai_generated_text", aiResponse);

            return TaskResultDTO.builder()
                    .status(StepStatus.SUCCESS)
                    .result(resultNode)
                    .build();

        } catch (Exception e) {
            log.error("❌ AI Generation Failed: {}", e.getMessage());
            return TaskResultDTO.builder()
                    .status(StepStatus.FAILED)
                    .errorMessage("AI API Error: " + e.getMessage())
                    .build();
        }
    }
}
