package com.zync.orchestratorservice.controller;

import com.zync.orchestratorservice.dto.WorkflowCreateRequestDTO;
import com.zync.orchestratorservice.dto.WorkflowUpdateRequestDTO;
import com.zync.orchestratorservice.service.WorkflowService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @DeleteMapping("/{triggerId}")
    public ResponseEntity<?> deleteWorkflow(@PathVariable String triggerId) {
        workflowService.deleteWorkflow(triggerId);
        return ResponseEntity.ok(Map.of("message", "Workflow deleted successfully"));
    }

    @PatchMapping("/{triggerId}/pause")
    public ResponseEntity<?> pauseWorkflow(@PathVariable String triggerId) {
        workflowService.pauseWorkflow(triggerId);
        return ResponseEntity.ok(Map.of("message", "Workflow paused successfully"));
    }

    @PatchMapping("/{triggerId}/resume")
    public ResponseEntity<?> resumeWorkflow(@PathVariable String triggerId) {
        workflowService.resumeWorkflow(triggerId);
        return ResponseEntity.ok(Map.of("message", "Workflow resumed successfully"));
    }

    @PutMapping("/{triggerId}")
    public ResponseEntity<?> updateWorkflow(
            @PathVariable String triggerId,
            @RequestBody @Valid WorkflowUpdateRequestDTO request) {
        workflowService.updateWorkflow(triggerId, request);
        return ResponseEntity.ok(Map.of("message", "Workflow updated successfully"));
    }
}
