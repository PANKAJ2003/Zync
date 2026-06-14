package com.zync.orchestratorservice.consumer;

import com.zync.domain.enums.RunStatus;
import com.zync.domain.events.WebhookEvent;
import com.zync.orchestratorservice.entity.ExecutionRun;
import com.zync.orchestratorservice.repository.ExecutionRunRepository;
import com.zync.orchestratorservice.repository.WorkflowRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j // Lombok annotation to give us the 'log' object automatically!
@Service
public class WebhookEventConsumer {

    private final WorkflowRepository workflowRepository;
    private final ExecutionRunRepository executionRunRepository;

    public WebhookEventConsumer(WorkflowRepository workflowRepository, ExecutionRunRepository executionRunRepository) {
        this.workflowRepository = workflowRepository;
        this.executionRunRepository = executionRunRepository;
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

                    ExecutionRun executionRun = new ExecutionRun();
                    executionRun.setId(UUID.fromString(event.getTraceId()));
                    executionRun.setWorkflow(workflow);
                    executionRun.setTriggerPayload(event.getPayload());
                    executionRun.setStatus(RunStatus.RUNNING);
                    executionRunRepository.save(executionRun);

                    log.info("Execution Run {} saved to database!", executionRun.getId());
                },
                () -> {
                    log.warn("❌ FAILED: No workflow exists in the database for Trigger ID: {}", event.getWebhookId());
                    // In the real world, we just drop the message or put it in a DLQ
                }
        );
        log.info("=================================================\n");
    }
}