package com.zync.executorservice.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.zync.domain.enums.StepStatus;
import com.zync.executorservice.dto.TaskResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ActionRouter {
    private final Map<String, AppActionAdapter> adapters;


    public ActionRouter(List<AppActionAdapter> adaptorList) {
        this.adapters = adaptorList.stream()
                .collect(Collectors.toMap(AppActionAdapter::getActionType, adaptor -> adaptor));
    }

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
