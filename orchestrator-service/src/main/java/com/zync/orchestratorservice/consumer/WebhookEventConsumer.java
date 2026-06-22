package com.zync.orchestratorservice.consumer;

import com.zync.domain.enums.RunStatus;
import com.zync.domain.enums.WorkflowStatus;
import com.zync.domain.events.TaskExecutionEvent;
import com.zync.domain.events.WebhookEvent;
import com.zync.orchestratorservice.entity.ExecutionRun;
import com.zync.orchestratorservice.entity.WorkflowStep;
import com.zync.orchestratorservice.repository.ExecutionRunRepository;
import com.zync.orchestratorservice.repository.WorkflowRepository;
import com.zync.orchestratorservice.repository.WorkflowStepRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j // Lombok annotation to give us the 'log' object automatically!
@Service
public class WebhookEventConsumer {

    private final WorkflowRepository workflowRepository;
    private final ExecutionRunRepository executionRunRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final KafkaTemplate<String, TaskExecutionEvent> kafkaTemplate;

    public WebhookEventConsumer(WorkflowRepository workflowRepository,
                                ExecutionRunRepository executionRunRepository,
                                WorkflowStepRepository workflowStepRepository,
                                KafkaTemplate<String, TaskExecutionEvent> kafkaTemplate) {
        this.workflowRepository = workflowRepository;
        this.executionRunRepository = executionRunRepository;
        this.workflowStepRepository = workflowStepRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    // Listens to the exact topic the Ingestion Service writes to
    @KafkaListener(topics = "incoming-webhooks", groupId = "zync-orchestrator-group")
    public void consumeWebhook(WebhookEvent event) {

        log.info("=================================================");
        log.info("📩 NEW EVENT RECEIVED FROM KAFKA!");
        log.info("Trace ID: {}", event.getTraceId());
        log.info("Webhook ID: {}", event.getWebhookId());
        log.info("Raw Payload: {}", event.getPayload().toString());

        // Now we ask the Database: "Does anyone own this Webhook ID?"
        workflowRepository.findByTriggerId(event.getWebhookId()).ifPresentOrElse(
                workflow -> {
                    log.info("✅ SUCCESS! Found matching workflow: {}", workflow.getName());

                    if (workflow.getStatus() == WorkflowStatus.PAUSED) {
                        log.info("Workflow is paused. Skipping execution.");
                        return;
                    }

                    UUID trackingId = UUID.fromString(event.getTraceId());

                    ExecutionRun executionRun = new ExecutionRun();
                    executionRun.setId(trackingId);
                    executionRun.setWorkflow(workflow);
                    executionRun.setTriggerPayload(event.getPayload());
                    executionRun.setStatus(RunStatus.RUNNING);
                    executionRunRepository.save(executionRun);

                    log.info("Execution Run {} saved to database!", executionRun.getId());

                    getWorkflowStep(workflow.getId(), 1).ifPresentOrElse(
                            step -> {
                                TaskExecutionEvent taskEvent = TaskExecutionEvent.builder()
                                        .stepId(step.getId())
                                        .webhookPayload(event.getPayload())
                                        .actionType(step.getActionType())
                                        .stepConfig(step.getConfiguration())
                                        .executionRunId(executionRun.getId())
                                        .build();

                                kafkaTemplate.send("task-execution", trackingId.toString(), taskEvent);

                                log.info("Task step1 {} sent to Kafka!", trackingId);
                            },
                            () -> log.warn("⚠️ No Step 1 found for workflow {}", workflow.getId())
                    );

                },
                () -> {
                    log.warn("❌ FAILED: No workflow exists in the database for Trigger ID: {}", event.getWebhookId());
                }
        );
        log.info("=================================================\n");
    }


    private Optional<WorkflowStep> getWorkflowStep(UUID workflowId, int stepOrder) {
        return workflowStepRepository.getStepByWorkflowIdAndStepOrder(workflowId, stepOrder);
    }
}