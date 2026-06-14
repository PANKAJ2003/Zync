package com.zync.orchestratorservice.controller;

import com.zync.orchestratorservice.dto.WorkflowCreateRequestDTO;
import com.zync.orchestratorservice.service.WorkflowService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/workflows")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping
    public ResponseEntity<?> createWorkflow(
            @RequestBody @Valid WorkflowCreateRequestDTO request) {

        String triggerId = workflowService.createWorkflow(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Workflow created successfully!",
                "webhookUrl", "http://localhost:8081/api/v1/webhooks/" + triggerId
        ));
    }
}
