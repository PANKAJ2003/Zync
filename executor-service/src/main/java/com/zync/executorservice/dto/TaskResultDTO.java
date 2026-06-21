package com.zync.executorservice.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.zync.domain.enums.StepStatus;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskResultDTO {

    private StepStatus status;
    private String errorMessage;
    private JsonNode result;
}