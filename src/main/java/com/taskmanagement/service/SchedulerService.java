package com.taskmanagement.service;

import com.taskmanagement.entity.Rundown;
import com.taskmanagement.repository.RundownRepository;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Scheduler service for timed task execution
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private final RundownRepository rundownRepository;
    private final ExecutionService executionService;

    /**
     * Check and execute scheduled rundowns every minute
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkScheduledRundowns() {
        log.debug("Checking for scheduled rundowns...");
        
        List<Rundown> scheduledRundowns = rundownRepository
                .findByScheduleTypeInAndScheduleStatusAndNextRunTimeBefore(
                        Arrays.asList(Rundown.SCHEDULE_ONCE, Rundown.SCHEDULE_CRON),
                        Rundown.SCHEDULE_STATUS_SCHEDULED,
                        LocalDateTime.now()
                );

        for (Rundown rundown : scheduledRundowns) {
            log.info("Triggering scheduled rundown: {} ({})", rundown.getRundownCode(), rundown.getId());
            
            try {
                // Update status to running
                rundown.setScheduleStatus(Rundown.SCHEDULE_STATUS_RUNNING);
                rundownRepository.save(rundown);
                
                // Execute rundown
                executionService.executeRundown(rundown.getId());
                
                // Update schedule status after execution
                if (Rundown.SCHEDULE_ONCE.equals(rundown.getScheduleType())) {
                    // One-time: mark completed
                    rundown.setScheduleStatus(Rundown.SCHEDULE_STATUS_COMPLETED);
                } else if (Rundown.SCHEDULE_CRON.equals(rundown.getScheduleType())) {
                    // Cron: calculate next run time
                    calculateNextRunTime(rundown);
                }
                
                rundown.setLastRunAt(LocalDateTime.now());
                rundownRepository.save(rundown);
                
            } catch (Exception e) {
                log.error("Error executing scheduled rundown: {}", rundown.getId(), e);
                rundown.setScheduleStatus(Rundown.SCHEDULE_STATUS_SCHEDULED);
                rundownRepository.save(rundown);
            }
        }
    }

    /**
     * Schedule a rundown for one-time execution
     */
    @Transactional
    public void scheduleOnce(Long rundownId, LocalDateTime runTime) {
        log.info("Scheduling rundown {} for one-time execution at {}", rundownId, runTime);

        Rundown rundown = rundownRepository.findById(rundownId)
                .orElseThrow(() -> new RuntimeException("Rundown not found: " + rundownId));

        rundown.setScheduleType(Rundown.SCHEDULE_ONCE);
        rundown.setRunTime(runTime);
        rundown.setScheduleStatus(Rundown.SCHEDULE_STATUS_SCHEDULED);
        rundown.setNextRunTime(runTime);

        rundownRepository.save(rundown);
    }

    /**
     * Schedule a rundown for cron execution
     */
    @Transactional
    public void scheduleCron(Long rundownId, String cronExpression) {
        log.info("Scheduling rundown {} for cron execution: {}", rundownId, cronExpression);

        Rundown rundown = rundownRepository.findById(rundownId)
                .orElseThrow(() -> new RuntimeException("Rundown not found: " + rundownId));

        // Validate cron expression
        try {
            CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
            Cron cron = parser.parse(cronExpression);
            
            rundown.setScheduleType(Rundown.SCHEDULE_CRON);
            rundown.setCronExpression(cronExpression);
            rundown.setScheduleStatus(Rundown.SCHEDULE_STATUS_SCHEDULED);
            
            // Calculate next run time
            ExecutionTime executionTime = ExecutionTime.forCron(cron);
            ZonedDateTime nextExecution = executionTime.nextExecution(ZonedDateTime.now()).orElse(null);
            rundown.setNextRunTime(nextExecution != null ? nextExecution.toLocalDateTime() : null);

            rundownRepository.save(rundown);
            
        } catch (Exception e) {
            throw new RuntimeException("Invalid cron expression: " + cronExpression, e);
        }
    }

    /**
     * Cancel scheduled execution
     */
    @Transactional
    public void cancelSchedule(Long rundownId) {
        log.info("Cancelling schedule for rundown: {}", rundownId);

        Rundown rundown = rundownRepository.findById(rundownId)
                .orElseThrow(() -> new RuntimeException("Rundown not found: " + rundownId));

        rundown.setScheduleType(Rundown.SCHEDULE_NONE);
        rundown.setScheduleStatus(Rundown.SCHEDULE_STATUS_CANCELLED);
        rundown.setNextRunTime(null);
        rundown.setCronExpression(null);
        rundown.setRunTime(null);

        rundownRepository.save(rundown);
    }

    /**
     * Calculate next run time for cron expression
     */
    private void calculateNextRunTime(Rundown rundown) {
        if (rundown.getCronExpression() == null) {
            return;
        }

        try {
            CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
            Cron cron = parser.parse(rundown.getCronExpression());
            ExecutionTime executionTime = ExecutionTime.forCron(cron);
            
            ZonedDateTime nextExecution = executionTime.nextExecution(ZonedDateTime.now()).orElse(null);
            rundown.setNextRunTime(nextExecution != null ? nextExecution.toLocalDateTime() : null);
            
        } catch (Exception e) {
            log.error("Error calculating next run time", e);
            rundown.setNextRunTime(null);
        }
    }
}
