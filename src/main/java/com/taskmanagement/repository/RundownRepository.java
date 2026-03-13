package com.taskmanagement.repository;

import com.taskmanagement.entity.Rundown;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RundownRepository extends JpaRepository<Rundown, Long> {

    Optional<Rundown> findByRundownCode(String rundownCode);

    Page<Rundown> findByCreatedBy(Long createdBy, Pageable pageable);

    Page<Rundown> findByStatus(String status, Pageable pageable);

    Page<Rundown> findByTemplateId(Long templateId, Pageable pageable);

    // Find scheduled tasks ready to execute
    @Query("SELECT r FROM Rundown r WHERE r.scheduleType IN :types AND r.scheduleStatus = :status AND r.nextRunTime <= :now")
    List<Rundown> findByScheduleTypeInAndScheduleStatusAndNextRunTimeBefore(
            @Param("types") List<String> types,
            @Param("status") String status,
            @Param("now") LocalDateTime now
    );

    // Find by schedule status
    List<Rundown> findByScheduleStatus(String scheduleStatus);

    // Find today's rundown code max number
    @Query("SELECT MAX(r.rundownCode) FROM Rundown r WHERE r.rundownCode LIKE :prefix%")
    Optional<String> findMaxRundownCodeByPrefix(@Param("prefix") String prefix);
}
