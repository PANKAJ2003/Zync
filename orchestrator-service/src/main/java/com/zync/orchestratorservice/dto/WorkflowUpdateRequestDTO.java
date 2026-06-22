package com.zync.orchestratorservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record WorkflowUpdateRequestDTO(

        @NotBlank(message = "Name is required")
        String name,

        String description,

        @NotEmpty(message = "Workflow must have at least 1 step")
        List<@Valid StepRequestDTO> steps) {
}
