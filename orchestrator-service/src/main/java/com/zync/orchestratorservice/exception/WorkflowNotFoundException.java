package com.zync.orchestratorservice.exception;

public class WorkflowNotFoundException extends RuntimeException {

    public WorkflowNotFoundException(String triggerId) {
        super("Workflow not found with trigger ID: " + triggerId);
    }
}
