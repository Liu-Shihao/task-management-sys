package com.taskmanagement.repository;

import com.taskmanagement.entity.Task;
import com.taskmanagement.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByRundownIdOrderBySequenceOrderAsc(Long rundownId);

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByRundownIdAndStatus(Long rundownId, TaskStatus status);
}
