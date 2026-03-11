package com.taskmanagement.repository;

import com.taskmanagement.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {

    @Query("SELECT t FROM Template t WHERE t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    List<Template> findAllActive();

    @Query("SELECT t FROM Template t LEFT JOIN FETCH t.tasks WHERE t.id = :id AND t.deletedAt IS NULL")
    Template findByIdWithTasks(Long id);
}
