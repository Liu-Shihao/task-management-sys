package com.finblock.tms.modules.executionlog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExecutionLogRepository extends JpaRepository<ExecutionLogEntity, Long> {
    List<ExecutionLogEntity> findByTaskIdOrderByIdAsc(Long taskId);
}

