package com.taskmanagement.repository;

import com.taskmanagement.entity.Task;
import com.taskmanagement.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByRundownCodeOrderBySequenceNoAsc(String rundownCode);

    /**
     * 查找所有到期需要执行的任务
     * 条件：
     * 1. 调度已启用
     * 2. 下次执行时间 <= 当前时间
     * 3. 任务状态为 PENDING
     */
    @Query("SELECT t FROM Task t WHERE t.schedulingEnabled = true " +
           "AND t.nextRunAt IS NOT NULL " +
           "AND t.nextRunAt <= :now " +
           "AND t.status = :status")
    List<Task> findTasksToExecute(@Param("now") Instant now, @Param("status") TaskStatus status);
}
