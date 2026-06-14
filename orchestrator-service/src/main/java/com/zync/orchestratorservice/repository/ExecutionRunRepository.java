package com.zync.orchestratorservice.repository;

import com.zync.orchestratorservice.entity.ExecutionRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ExecutionRunRepository extends JpaRepository<ExecutionRun, UUID> {
}
