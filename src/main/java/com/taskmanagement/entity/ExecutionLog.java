package com.taskmanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * ExecutionLog entity - audit trail for task execution
 */
@Entity
@Table(name = "execution_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "execution_id", length = 255)
    private String executionId;

    @Column(nullable = false, length = 20)
    private String status;  // success, failed, aborted

    @Column(name = "output", columnDefinition = "LONGTEXT")
    private String output;

    @Column(name = "error_output", columnDefinition = "LONGTEXT")
    private String errorOutput;

    @Column(name = "duration_ms")
    private Integer durationMs;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // JPA callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
