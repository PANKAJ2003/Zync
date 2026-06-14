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

@Service
public class WorkflowService {
    private final WorkflowRepository workflowRepository;
    private final WorkflowStepRepository stepRepository;

    public WorkflowService(WorkflowRepository workflowRepository, WorkflowStepRepository stepRepository) {
        this.workflowRepository = workflowRepository;
        this.stepRepository = stepRepository;
    }


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
