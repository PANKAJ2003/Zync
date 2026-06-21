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

@Slf4j
@Component
public class TaskConsumer {

    private final ActionRouter actionRouter;
    private final KafkaTemplate<String, TaskResultEvent> kafkaTemplate;

    public TaskConsumer(ActionRouter actionRouter, KafkaTemplate<String, TaskResultEvent> kafkaTemplate) {
        this.actionRouter = actionRouter;
        this.kafkaTemplate = kafkaTemplate;
    }

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

    public void sentTaskResult(TaskResultEvent result) {
        String ORCHESTRATOR_TASK_RESULT_TOPIC = "task-execution-result";
        kafkaTemplate.send(ORCHESTRATOR_TASK_RESULT_TOPIC, result.getExecutionRunId().toString(), result);
    }
}
