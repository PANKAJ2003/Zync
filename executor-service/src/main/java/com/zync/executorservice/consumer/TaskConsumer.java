package com.zync.executorservice.consumer;

import com.zync.domain.enums.StepStatus;
import com.zync.domain.events.TaskExecutionEvent;
import com.zync.domain.events.TaskResultEvent;
import com.zync.executorservice.core.ActionRouter;
import com.zync.executorservice.dto.TaskResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Consumer that listens for task execution events from the "task-execution" Kafka topic
 * and processes them by routing to the appropriate action handler. After execution,
 * the result is published to the "task-execution-result" topic for the orchestrator
 * to pick up and continue the workflow.
 */
@Slf4j
@Component
public class TaskConsumer {

    private final ActionRouter actionRouter;
    private final KafkaTemplate<String, TaskResultEvent> kafkaTemplate;

    public TaskConsumer(ActionRouter actionRouter, KafkaTemplate<String, TaskResultEvent> kafkaTemplate) {
        this.actionRouter = actionRouter;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Consumes a task execution event from the "task-execution" topic.
     * Routes the task to the appropriate action based on its type, builds a result event
     * with the outcome (success result or error message), and publishes it to the
     * orchestrator's result topic.
     *
     * @param task the task execution event containing run ID, step ID, action type,
     *             step configuration, and webhook payload
     */
    @KafkaListener(topics = "task-execution", groupId = "executor-service")
    public void consume(TaskExecutionEvent task) {

        log.info("=================================================");
        log.info("EXECUTOR WOKE UP! Run ID: {}", task.getExecutionRunId());

        TaskResultDTO taskStatus = actionRouter.routeAndExecute(task.getActionType(), task.getStepConfig(), task.getWebhookPayload());

        TaskResultEvent result = TaskResultEvent.builder()
                .executionRunId(task.getExecutionRunId())
                .stepId(task.getStepId())
                .actionType(task.getActionType())
                .status(taskStatus.getStatus())
                .build();

        if (taskStatus.getStatus() == StepStatus.SUCCESS) {
            log.info("Task completed successfully --- Send next task");
            result.setResult(taskStatus.getResult());
        } else {
            log.error("Task failed");
            result.setErrorMessage(taskStatus.getErrorMessage());
        }
        log.info("=================================================");

        sentTaskResult(result);
    }

    /**
     * Publishes a task result event to the "task-execution-result" topic keyed by
     * the execution run ID so the orchestrator can resume the workflow.
     *
     * @param result the result event to send
     */
    public void sentTaskResult(TaskResultEvent result) {
        String ORCHESTRATOR_TASK_RESULT_TOPIC = "task-execution-result";
        kafkaTemplate.send(ORCHESTRATOR_TASK_RESULT_TOPIC, result.getExecutionRunId().toString(), result);
    }
}
