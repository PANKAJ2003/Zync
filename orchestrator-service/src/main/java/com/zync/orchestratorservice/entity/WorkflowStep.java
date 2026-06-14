package com.zync.orchestratorservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workflow_steps")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowStep {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Links back to the parent workflow
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "action_type", nullable = false)
    private String actionType; // e.g., "SLACK_MESSAGE"

    // THE MAGIC: Stores dynamic schema-less JSON securely in Postgres!
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "configuration", columnDefinition = "jsonb", nullable = false)
    private JsonNode configuration;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}