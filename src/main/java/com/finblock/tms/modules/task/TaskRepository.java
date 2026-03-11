package com.finblock.tms.modules.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    List<TaskEntity> findByRundownIdOrderByIdAsc(Long rundownId);
}

