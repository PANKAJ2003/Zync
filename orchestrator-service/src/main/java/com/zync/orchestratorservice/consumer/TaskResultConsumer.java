package com.zync.orchestratorservice.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zync.domain.enums.RunStatus;
import com.zync.domain.enums.StepStatus;
import com.zync.domain.events.TaskExecutionEvent;
import com.zync.domain.events.TaskResultEvent;
import com.zync.orchestratorservice.entity.ExecutionLog;
import com.zync.orchestratorservice.entity.ExecutionRun;
import com.zync.orchestratorservice.repository.ExecutionLogRepository;
import com.zync.orchestratorservice.repository.ExecutionRunRepository;
import com.zync.orchestratorservice.repository.WorkflowStepRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Consumes task execution results from the {@code task-execution-result} topic,
 * logs each step outcome, and either dispatches the next step or marks the
 * workflow run as completed or failed.
 */
@Slf4j
@Component
public class TaskResultConsumer {

    private final String ORCHESTRATOR_TASK_RESULT_TOPIC = "task-execution-result";

    private final ExecutionRunRepository executionRunRepository;
    private final WorkflowStepRepository stepRepository;
    private final ExecutionLogRepository executionLogRepository;
    private final KafkaTemplate<String, TaskExecutionEvent> kafkaTemplate;

    public TaskResultConsumer(ExecutionRunRepository executionRunRepository, WorkflowStepRepository stepRepository, ExecutionLogRepository executionLogRepository, KafkaTemplate<String, TaskExecutionEvent> kafkaTemplate) {
        this.executionRunRepository = executionRunRepository;
        this.stepRepository = stepRepository;
        this.executionLogRepository = executionLogRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Processes the result of a completed task, updates the execution log, and determines the next
     * steps in the workflow. If the current step fails, it marks the corresponding execution run
     * as failed and stops further execution. If the step succeeds, it identifies the next step
     * in the workflow and triggers its execution. If no further steps exist, it marks the run
     * as completed.
     *
     * @param result The result of a completed task, including metadata such as execution run ID,
     *               step ID, action type, execution status, and any error messages or result data.
     */
    @KafkaListener(topics = ORCHESTRATOR_TASK_RESULT_TOPIC, groupId = "zync-orchestrator-group")
    public void consumeResult(TaskResultEvent result) {
        log.info("ORCHESTRATOR HEARD BACK FROM EXECUTOR! Step: {} | Status: {}", result.getActionType(), result.getStatus());

        // Fetch run and step from db
        ExecutionRun run = executionRunRepository.findById(result.getExecutionRunId()).orElseThrow();
        var step = stepRepository.findById(result.getStepId()).orElseThrow();

        // Save the result in the execution log
        ExecutionLog logEntry = ExecutionLog.builder()
                .executionRun(run)
                .step(step)
                .status(result.getStatus())
                .errorMessage(result.getErrorMessage())
                .build();

        executionLogRepository.save(logEntry);

        if (result.getStatus() == StepStatus.FAILED) {
            log.error("Step failed! Stoping further step execution");
            run.setStatus(RunStatus.FAILED);
            executionRunRepository.save(run);
            return;
        }

        int nextStepOrder = step.getStepOrder() + 1;

        stepRepository.getStepByWorkflowIdAndStepOrder(run.getWorkflow().getId(), nextStepOrder)
                .ifPresentOrElse(
                        nextStep -> {
                            log.info("Found Step {}: {}. Sending to Executor...", nextStepOrder, nextStep.getActionType());

                            TaskExecutionEvent nextTask = TaskExecutionEvent.builder()
                                    .stepId(nextStep.getId())
                                    .actionType(nextStep.getActionType())
                                    .stepConfig(nextStep.getConfiguration())
                                    .executionRunId(run.getId())
                                    .webhookPayload(appendStepResultToPayload(step.getStepOrder(), result.getResult(), run.getTriggerPayload()))
                                    .build();

                            kafkaTemplate.send("task-execution", run.getId().toString(), nextTask);
                        },
                        () -> {
                            log.info("No more steps found for workflow {}. Marking run as completed...", run.getWorkflow().getId());
                            run.setStatus(RunStatus.COMPLETED);
                            run.setCompletedAt(Instant.now());
                            executionRunRepository.save(run);
                        }
                );
    }

    private JsonNode appendStepResultToPayload(int stepOrder, JsonNode stepResult, JsonNode currentPayload) {

        // If payload isn't a JSON Object (e.g., it's a raw string/array), we can't easily append to it.
        if (!currentPayload.isObject()) {
            return currentPayload;
        }

        ObjectNode resultNode = (ObjectNode) currentPayload.deepCopy();

        if (stepResult != null && !stepResult.isNull()) {
            resultNode.set("step_" + stepOrder, stepResult);
        }

        return resultNode;
    }
}
