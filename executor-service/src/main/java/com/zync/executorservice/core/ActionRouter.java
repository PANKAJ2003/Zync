package com.zync.executorservice.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.zync.domain.enums.StepStatus;
import com.zync.executorservice.dto.TaskResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Routes workflow steps to the appropriate {@link AppActionAdapter} based on
 * the action type. Uses constructor injection to build a strategy map from
 * all available adapter beans.
 */
@Slf4j
@Service
public class ActionRouter {
    private final Map<String, AppActionAdapter> adapters;


    public ActionRouter(List<AppActionAdapter> adaptorList) {
        this.adapters = adaptorList.stream()
                .collect(Collectors.toMap(AppActionAdapter::getActionType, adaptor -> adaptor));
    }

    /**
     * Looks up the adapter for the given action type and delegates execution.
     * Returns a FAILED result if no adapter is registered for the type.
     *
     * @param actionType the action type key (e.g., HTTP_REQUEST, AI_PROMPT)
     * @param config     step-specific configuration
     * @param payload    the webhook payload for template resolution
     * @return the execution result with status, result data, or error
     */
    public TaskResultDTO routeAndExecute(String actionType, JsonNode config, JsonNode payload) {
        AppActionAdapter adapter = adapters.get(actionType);

        if (adapter == null) {
            log.error("❌ No integration found for Action Type: {}", actionType);
            String error = "No integration found for Action type: " + actionType;
            return TaskResultDTO.builder()
                    .status(StepStatus.FAILED)
                    .errorMessage(error)
                    .build();
        }

        log.info("⚙️ Routing task to adapter: {}", adapter.getClass().getSimpleName());
        return adapter.execute(config, payload);
    }
}
