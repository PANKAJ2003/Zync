package com.zync.orchestratorservice.repository;

import com.zync.orchestratorservice.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, UUID> {

    Optional<Workflow> findByTriggerId(String triggerId);
}