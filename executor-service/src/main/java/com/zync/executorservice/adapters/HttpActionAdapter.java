package com.zync.executorservice.adapters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zync.domain.enums.StepStatus;
import com.zync.executorservice.core.AppActionAdapter;
import com.zync.executorservice.dto.TaskResultDTO;
import com.zync.executorservice.utils.TemplateResolver;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class HttpActionAdapter implements AppActionAdapter {

    private final RestClient restClient;

    public HttpActionAdapter() {
        this.restClient = RestClient.create();
    }

    @Override
    public String getActionType() {
        return "HTTP_REQUEST";
    }

    /**
     * Executes an HTTP request based on the given configuration and webhook payload.
     * <p>
     * This method reads the HTTP URL, method, and body parameters from the provided step configuration,
     * resolves any templated placeholders against the webhook payload, and sends an HTTP request.
     * The response status and body are returned as part of a TaskResultDTO object.
     *
     * @param stepConfig     The configuration for the HTTP request, including URL, HTTP method, and optional body.
     *                       This parameter should be a valid JsonNode containing the keys "url" and "method",
     *                       and optionally "body".
     * @param webhookPayload The payload used to resolve placeholders within the step configuration.
     *                       This parameter contains dynamic values required to generate the final HTTP request.
     * @return A TaskResultDTO object containing the status of the request (SUCCESS or FAILED),
     * and the HTTP response status code and body as part of the result.
     */
    @Override
    @Retry(name = "httpAdapterRetry", fallbackMethod = "httpFallback")
    public TaskResultDTO execute(JsonNode stepConfig, JsonNode webhookPayload) {

        String url = stepConfig.path("url").asText();
        String method = stepConfig.path("method").asText();
        String rawBody = stepConfig.path("body").isMissingNode() ? "" : stepConfig.path("body").asText();

        String resolvedUrl = TemplateResolver.resolve(url, webhookPayload);
        String resolvedBody = TemplateResolver.resolve(rawBody, webhookPayload);

        log.info("🌐 Sending HTTP request to: {}", resolvedUrl);

        var requestBodySpec = restClient.method(HttpMethod.valueOf(method.toUpperCase()))
                .uri(resolvedUrl)
                .contentType(MediaType.APPLICATION_JSON);

        if (!resolvedBody.isEmpty()) {
            requestBodySpec = requestBodySpec.body(resolvedBody);
        }

        ResponseEntity<String> response = requestBodySpec.retrieve().toEntity(String.class);

        log.info("HTTP Response Status: {}", response.getStatusCode());

        ObjectNode resultNode = JsonNodeFactory.instance.objectNode();
        resultNode.put("statusCode", response.getStatusCode().value());
        resultNode.put("body", response.getBody() != null ? response.getBody() : "");

        return TaskResultDTO.builder()
                .status(StepStatus.SUCCESS)
                .result(resultNode)
                .build();
    }

    /**
     * Handles the fallback logic for HTTP calls that fail after multiple retries.
     * This method logs the error and returns a TaskResultDTO indicating the failure.
     *
     * @param stepConfig     The configuration for the HTTP request, which includes details
     *                       such as the URL, method, and optional body. This parameter
     *                       provides the context for the failed operation.
     * @param webhookPayload The dynamic payload, used to resolve any placeholders
     *                       in the HTTP request configuration, which was applied
     *                       during the initial request.
     * @param e              The exception encountered during the failure of the HTTP request,
     *                       containing details of the error that caused the retries to fail.
     * @return A TaskResultDTO object containing the status set to FAILED and an error
     * message describing the failure.
     */
    public TaskResultDTO httpFallback(JsonNode stepConfig, JsonNode webhookPayload, Exception e) {
        log.error("❌ HTTP Call completely failed after 3 retries! Error: {}", e.getMessage());

        return TaskResultDTO.builder()
                .status(StepStatus.FAILED)
                .errorMessage("API Call Failed: " + e.getMessage())
                .build();
    }
}