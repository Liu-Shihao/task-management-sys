package com.taskmanagement.util;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * Cron 表达式工具类
 * 支持 Quartz 格式的 cron 表达式
 */
public class CronExpressionUtils {

    private static final CronParser PARSER = new CronParser(
            CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ)
    );

    private CronExpressionUtils() {
        // 工具类禁止实例化
    }

    /**
     * 获取当前时间之后的下一次执行时间
     *
     * @param cronExpression Quartz 格式的 cron 表达式
     * @return 下一次执行时间，如果无法解析则返回 null
     */
    public static Instant getNextExecution(String cronExpression) {
        if (cronExpression == null || cronExpression.isBlank()) {
            return null;
        }
        try {
            Cron cron = PARSER.parse(cronExpression);
            ExecutionTime executionTime = ExecutionTime.forCron(cron);
            Optional<ZonedDateTime> next = executionTime.nextExecution(ZonedDateTime.now());
            return next.map(ZonedDateTime::toInstant).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取指定时间之后的下一次执行时间
     *
     * @param cronExpression Quartz 格式的 cron 表达式
     * @param after          参考时间
     * @return 下一次执行时间，如果无法解析则返回 null
     */
    public static Instant getNextExecutionAfter(String cronExpression, Instant after) {
        if (cronExpression == null || cronExpression.isBlank() || after == null) {
            return null;
        }
        try {
            Cron cron = PARSER.parse(cronExpression);
            ExecutionTime executionTime = ExecutionTime.forCron(cron);
            Optional<ZonedDateTime> next = executionTime.nextExecution(after.atZone(ZoneId.systemDefault()));
            return next.map(ZonedDateTime::toInstant).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证 cron 表达式是否有效
     *
     * @param cronExpression cron 表达式
     * @return 是否有效
     */
    public static boolean isValid(String cronExpression) {
        if (cronExpression == null || cronExpression.isBlank()) {
            return false;
        }
        try {
            PARSER.parse(cronExpression);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
