package com.zync.orchestratorservice.controller;

import com.zync.orchestratorservice.dto.WorkflowCreateRequestDTO;
import com.zync.orchestratorservice.dto.WorkflowUpdateRequestDTO;
import com.zync.orchestratorservice.service.WorkflowService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST API for managing workflow lifecycle. Exposes endpoints to create,
 * update, delete, pause, and resume workflow definitions.
 */
@RestController
@RequestMapping("/api/v1/workflows")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    /**
     * Creates a new workflow with steps and returns the webhook trigger URL.
     *
     * @param request validated workflow definition with name and ordered steps
     * @return 201 CREATED with the webhook URL for external callers
     */
    @PostMapping
    public ResponseEntity<?> createWorkflow(
            @RequestBody @Valid WorkflowCreateRequestDTO request) {

        String triggerId = workflowService.createWorkflow(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Workflow created successfully!",
                "webhookUrl", "http://localhost:8081/api/v1/webhooks/" + triggerId
        ));
    }

    /**
     * Deletes a workflow and its associated steps by trigger ID.
     *
     * @param triggerId the unique webhook trigger identifier
     * @return 200 OK with confirmation message
     */
    @DeleteMapping("/{triggerId}")
    public ResponseEntity<?> deleteWorkflow(@PathVariable String triggerId) {
        workflowService.deleteWorkflow(triggerId);
        return ResponseEntity.ok(Map.of("message", "Workflow deleted successfully"));
    }

    /**
     * Pauses an active workflow so incoming webhooks are skipped.
     *
     * @param triggerId the unique webhook trigger identifier
     * @return 200 OK with confirmation message
     */
    @PatchMapping("/{triggerId}/pause")
    public ResponseEntity<?> pauseWorkflow(@PathVariable String triggerId) {
        workflowService.pauseWorkflow(triggerId);
        return ResponseEntity.ok(Map.of("message", "Workflow paused successfully"));
    }

    /**
     * Resumes a paused workflow, re-enabling webhook processing.
     *
     * @param triggerId the unique webhook trigger identifier
     * @return 200 OK with confirmation message
     */
    @PatchMapping("/{triggerId}/resume")
    public ResponseEntity<?> resumeWorkflow(@PathVariable String triggerId) {
        workflowService.resumeWorkflow(triggerId);
        return ResponseEntity.ok(Map.of("message", "Workflow resumed successfully"));
    }

    /**
     * Replaces an existing workflow's definition and steps entirely.
     *
     * @param triggerId the unique webhook trigger identifier
     * @param request   validated updated workflow definition
     * @return 200 OK with confirmation message
     */
    @PutMapping("/{triggerId}")
    public ResponseEntity<?> updateWorkflow(
            @PathVariable String triggerId,
            @RequestBody @Valid WorkflowUpdateRequestDTO request) {
        workflowService.updateWorkflow(triggerId, request);
        return ResponseEntity.ok(Map.of("message", "Workflow updated successfully"));
    }
}
