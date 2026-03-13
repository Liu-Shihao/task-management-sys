package com.taskmanagement.repository;

import com.taskmanagement.entity.ExecutionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExecutionLogRepository extends JpaRepository<ExecutionLog, Long> {
    Page<ExecutionLog> findByTaskIdOrderByCreatedAtDesc(Long taskId, Pageable pageable);
}
