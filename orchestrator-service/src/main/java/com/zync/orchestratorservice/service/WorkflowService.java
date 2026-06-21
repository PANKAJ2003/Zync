package com.zync.orchestratorservice.service;

import com.zync.domain.enums.WorkflowStatus;
import com.zync.orchestratorservice.dto.WorkflowCreateRequestDTO;
import com.zync.orchestratorservice.entity.Workflow;
import com.zync.orchestratorservice.entity.WorkflowStep;
import com.zync.orchestratorservice.repository.WorkflowRepository;
import com.zync.orchestratorservice.repository.WorkflowStepRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service responsible for creating and managing workflows.
 * Handles the persistence of workflow definitions along with their
 * ordered steps, and generates unique webhook trigger IDs.
 */
@Service
public class WorkflowService {
    private final WorkflowRepository workflowRepository;
    private final WorkflowStepRepository stepRepository;

    public WorkflowService(WorkflowRepository workflowRepository, WorkflowStepRepository stepRepository) {
        this.workflowRepository = workflowRepository;
        this.stepRepository = stepRepository;
    }


    /**
     * Creates a new workflow with the given configuration. Generates a unique
     * trigger ID prefixed with "wh_", persists the workflow entity, and saves
     * all associated steps in order.
     *
     * @param request the workflow creation details including name, description,
     *                and ordered list of steps
     * @return the generated webhook trigger ID for the workflow
     */
    @Transactional
    public String createWorkflow(WorkflowCreateRequestDTO request) {

        // Generate a unique Webhook URL ID
        String triggerId = "wh_" + UUID.randomUUID();

        Workflow workflow = Workflow.builder()
                .triggerId(triggerId)
                .name(request.name())
                .description(request.description())
                .status(WorkflowStatus.ACTIVE)
                .build();

        workflowRepository.save(workflow);

        // Save the steps in the DB
        request.steps().forEach(step -> {
            WorkflowStep workflowStep = WorkflowStep.builder()
                    .workflow(workflow)
                    .stepOrder(step.stepOrder())
                    .actionType(step.actionType())
                    .configuration(step.config())
                    .build();
            stepRepository.save(workflowStep);
        });

        return triggerId;
    }

}
