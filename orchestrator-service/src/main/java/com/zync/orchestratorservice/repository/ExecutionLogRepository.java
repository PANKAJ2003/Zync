package com.zync.orchestratorservice.repository;

import com.zync.orchestratorservice.entity.ExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ExecutionLogRepository extends JpaRepository<ExecutionLog, UUID> {
}
