package com.taskmanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Rundown entity - task list that can be executed sequentially
 * Supports scheduling (one-time or cron)
 */
@Entity
@Table(name = "rundowns")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rundown {

    // Status constants
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_RUNNING = "running";
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_PARTIAL_SUCCESS = "partial_success";
    public static final String STATUS_FAILED = "failed";
    public static final String STATUS_ABORTED = "aborted";

    // Schedule type constants
    public static final String SCHEDULE_NONE = "none";
    public static final String SCHEDULE_ONCE = "once";
    public static final String SCHEDULE_CRON = "cron";

    // Schedule status constants
    public static final String SCHEDULE_STATUS_SCHEDULED = "scheduled";
    public static final String SCHEDULE_STATUS_RUNNING = "running";
    public static final String SCHEDULE_STATUS_COMPLETED = "completed";
    public static final String SCHEDULE_STATUS_CANCELLED = "cancelled";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rundown_code", nullable = false, unique = true, length = 50)
    private String rundownCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "template_id")
    private Long templateId;

    @Column(nullable = false, length = 20)
    private String status = "pending";  // pending, running, partial_success, success, aborted, failed

    @Column(name = "schedule_type", length = 20)
    private String scheduleType;  // none, once, cron

    @Column(name = "cron_expression", length = 100)
    private String cronExpression;

    @Column(name = "run_time")
    private LocalDateTime runTime;

    @Column(name = "schedule_status", length = 20)
    private String scheduleStatus;  // scheduled, running, completed, cancelled

    @Column(name = "next_run_time")
    private LocalDateTime nextRunTime;

    @Column(name = "last_run_at")
    private LocalDateTime lastRunAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "rundown", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    // Helper method to add task
    public void addTask(Task task) {
        tasks.add(task);
        task.setRundown(this);
    }

    // Helper method to remove task
    public void removeTask(Task task) {
        tasks.remove(task);
        task.setRundown(null);
    }

    // JPA callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
