package com.taskmanagement.repository;

import com.taskmanagement.entity.ReleaseRundown;
import com.taskmanagement.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReleaseRundownRepository extends JpaRepository<ReleaseRundown, Long> {

    @Query("SELECT r FROM ReleaseRundown r WHERE r.deletedAt IS NULL ORDER BY r.createdAt DESC")
    List<ReleaseRundown> findAllActive();

    @Query("SELECT r FROM ReleaseRundown r LEFT JOIN FETCH r.tasks WHERE r.id = :id AND r.deletedAt IS NULL")
    ReleaseRundown findByIdWithTasks(Long id);

    List<ReleaseRundown> findByStatus(TaskStatus status);
}
