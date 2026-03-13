package com.taskmanagement.service;

import com.taskmanagement.entity.Task;
import com.taskmanagement.enums.TaskStatus;
import com.taskmanagement.enums.TaskType;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.util.CronExpressionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * 任务调度服务
 * 使用 ShedLock + 轮询实现分布式定时任务
 * 
 * 支持：
 * 1. Onetime: scheduledTime 指定具体执行时间
 * 2. Recurring: cronExpression 指定 cron 表达式
 * 
 * 多 Pod 环境：ShedLock 确保只在一个 Pod 上执行
 */
@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class TaskSchedulerService {

    private final TaskRepository taskRepository;

    /**
     * 每分钟扫描并执行到期的任务
     * ShedLock 确保同一任务只在一个 Pod 执行
     */
    @Scheduled(cron = "0 * * * * ?")
    @SchedulerLock(
            name = "scanAndExecuteTasks",
            lockAtMostFor = "PT50S",
            lockAtLeastFor = "PT5S"
    )
    @Transactional
    public void scanAndExecuteTasks() {
        Instant now = Instant.now();
        log.debug("开始扫描待执行任务，当前时间: {}", now);

        List<Task> tasksToRun = taskRepository.findTasksToExecute(now, TaskStatus.PENDING);

        if (tasksToRun.isEmpty()) {
            return;
        }

        log.info("找到 {} 个待执行任务", tasksToRun.size());

        for (Task task : tasksToRun) {
            try {
                executeTask(task);
            } catch (Exception e) {
                log.error("执行任务失败: taskId={}, name={}", task.getId(), task.getName(), e);
                handleTaskFailure(task, e.getMessage());
            }
        }
    }

    /**
     * 执行单个任务
     */
    private void executeTask(Task task) {
        log.info("开始执行任务: taskId={}, name={}, type={}, scheduledType={}",
                task.getId(),
                task.getName(),
                task.getAutomationType(),
                task.getCronExpression() != null ? "RECURRING" : "ONETIME");

        // 更新任务状态
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setStartedAt(Instant.now());
        task.setLastScheduledRunAt(Instant.now());
        task.setLastMessage("Task execution started by scheduler.");
        taskRepository.save(task);

        try {
            executeAutomation(task);
            task.setStatus(TaskStatus.COMPLETED);
            task.setFinishedAt(Instant.now());
            task.setLastMessage("Task executed successfully.");
            log.info("任务执行成功: taskId={}", task.getId());
        } catch (Exception e) {
            log.error("任务执行异常: taskId={}", task.getId(), e);
            task.setStatus(TaskStatus.FAILED);
            task.setFinishedAt(Instant.now());
            task.setLastMessage("Task execution failed: " + e.getMessage());
        }

        // 计算下次执行时间
        calculateNextRunTime(task);
        taskRepository.save(task);
    }

    /**
     * 执行自动化任务
     */
    private void executeAutomation(Task task) {
        TaskType type = task.getAutomationType();
        
        switch (type) {
            case JENKINS -> {
                log.info("触发 Jenkins Job: {}", task.getAutomationTarget());
                task.setExternalExecutionId("JENKINS-" + System.currentTimeMillis());
                task.setExternalUrl("https://jenkins.example.com/job/" + task.getAutomationTarget());
            }
            case ANSIBLE -> {
                log.info("触发 Ansible Job: {}", task.getAutomationTarget());
                task.setExternalExecutionId("ANSIBLE-" + System.currentTimeMillis());
                task.setExternalUrl("https://ansible.example.com/job/" + task.getAutomationTarget());
            }
        }
    }

    /**
     * 计算下次执行时间
     */
    private void calculateNextRunTime(Task task) {
        if (task.getCronExpression() != null && task.isSchedulingEnabled()) {
            task.setNextRunAt(CronExpressionUtils.getNextExecution(task.getCronExpression()));
            log.info("下次执行时间: taskId={}, nextRunAt={}", task.getId(), task.getNextRunAt());
        } else {
            // Onetime 执行后关闭调度
            task.setSchedulingEnabled(false);
            task.setNextRunAt(null);
        }
    }

    /**
     * 处理任务失败
     */
    private void handleTaskFailure(Task task, String errorMessage) {
        task.setStatus(TaskStatus.FAILED);
        task.setFinishedAt(Instant.now());
        task.setLastMessage("Task execution failed: " + errorMessage);
        
        if (task.getCronExpression() == null) {
            task.setSchedulingEnabled(false);
            task.setNextRunAt(null);
        }
        
        taskRepository.save(task);
    }
}
