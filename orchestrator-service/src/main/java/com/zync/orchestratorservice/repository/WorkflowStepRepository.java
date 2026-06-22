package com.zync.orchestratorservice.repository;

import com.zync.orchestratorservice.entity.WorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, UUID> {
    Optional<WorkflowStep> getStepByWorkflowIdAndStepOrder(UUID workflowId, Integer stepOrder);

    @Modifying
    @Query("DELETE FROM WorkflowStep ws WHERE ws.workflow.id = :workflowId")
    void deleteByWorkflowId(@Param("workflowId") UUID workflowId);
}
