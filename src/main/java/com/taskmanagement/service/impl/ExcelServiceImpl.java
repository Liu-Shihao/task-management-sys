package com.taskmanagement.service.impl;

import com.taskmanagement.dto.ExcelTaskData;
import com.taskmanagement.dto.ExcelUploadResult;
import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.UploadLog;
import com.taskmanagement.enums.TaskStatus;
import com.taskmanagement.enums.TaskType;
import com.taskmanagement.enums.UploadStatus;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.repository.UploadLogRepository;
import com.taskmanagement.service.ExcelService;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ExcelServiceImpl implements ExcelService {

    /**
     * Excel 表头约定（第 1 行）：
     * 0: Rundown Code
     * 1: Rundown Name
     * 2: Sequence
     * 3: Task Name
     * 4: Description
     * 5: Automation Type (JENKINS / ANSIBLE)
     * 6: Automation Target
     * 7: Automation Params (JSON)
     * 8: Schedule Mode (IMMEDIATE / ONCE / CRON)
     * 9: Scheduled Time (yyyy-MM-dd HH:mm:ss) - for ONCE
     * 10: Cron Expression - for CRON
     */

    private static final int COL_RUNDOWN_CODE = 0;
    private static final int COL_RUNDOWN_NAME = 1;
    private static final int COL_SEQUENCE = 2;
    private static final int COL_TASK_NAME = 3;
    private static final int COL_DESCRIPTION = 4;
    private static final int COL_AUTOMATION_TYPE = 5;
    private static final int COL_AUTOMATION_TARGET = 6;
    private static final int COL_AUTOMATION_PARAMS = 7;
    private static final int COL_SCHEDULE_MODE = 8;
    private static final int COL_SCHEDULED_TIME = 9;
    private static final int COL_CRON_EXPRESSION = 10;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TaskRepository taskRepository;
    private final UploadLogRepository uploadLogRepository;

    public ExcelServiceImpl(TaskRepository taskRepository, UploadLogRepository uploadLogRepository) {
        this.taskRepository = taskRepository;
        this.uploadLogRepository = uploadLogRepository;
    }

    // 允许的文件扩展名
    private static final String[] ALLOWED_EXTENSIONS = {".xlsx", ".xls"};

    // 允许的 MIME 类型
    private static final String[] ALLOWED_CONTENT_TYPES = {
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // xlsx
            "application/vnd.ms-excel"                                           // xls
    };

    @Override
    @Transactional
    public ExcelUploadResult uploadAndParse(MultipartFile file) {
        ExcelUploadResult result = new ExcelUploadResult();
        UploadLog uploadLog = new UploadLog();
        uploadLog.setFileName(file.getOriginalFilename());

        //校验文件格式
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) {
            String msg = "File name is required.";
            result.addError(msg);
            uploadLog.setStatus(UploadStatus.FAILED);
            uploadLog.setErrorMessage(msg);
            uploadLogRepository.save(uploadLog);
            return result;
        }

        if (!isValidExtension(filename)) {
            String msg = "Invalid file extension. Allowed extensions: .xlsx, .xls";
            result.addError(msg);
            uploadLog.setStatus(UploadStatus.FAILED);
            uploadLog.setErrorMessage(msg);
            uploadLogRepository.save(uploadLog);
            return result;
        }

        if (!isValidContentType(file.getContentType())) {
            String msg = "Invalid file type. Allowed MIME types: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel";
            result.addError(msg);
            uploadLog.setStatus(UploadStatus.FAILED);
            uploadLog.setErrorMessage(msg);
            uploadLogRepository.save(uploadLog);
            return result;
        }

        if (file.isEmpty()) {
            String msg = "Uploaded file is empty.";
            result.addError(msg);
            uploadLog.setStatus(UploadStatus.FAILED);
            uploadLog.setErrorMessage(msg);
            uploadLogRepository.save(uploadLog);
            return result;
        }

        List<ExcelTaskData> rows = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                String msg = "Excel sheet is empty.";
                result.addError(msg);
                uploadLog.setStatus(UploadStatus.FAILED);
                uploadLog.setErrorMessage(msg);
                uploadLogRepository.save(uploadLog);
                return result;
            }

            // 从第 2 行开始（索引 1），跳过表头
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                ExcelTaskData data = parseRow(rowIndex + 1, row); // 显示给用户的行号从 1 开始
                rows.add(data);
            }
        } catch (Exception e) {
            String msg = "Failed to parse Excel file: " + e.getMessage();
            result.addError(msg);
            uploadLog.setStatus(UploadStatus.FAILED);
            uploadLog.setErrorMessage(msg);
            uploadLogRepository.save(uploadLog);
            return result;
        }

        boolean hasRowErrors = rows.stream().anyMatch(ExcelTaskData::hasErrors);
        if (hasRowErrors) {
            result.setTasks(rows);
            result.setSuccess(false);
            result.addError("Excel contains invalid rows. See row errors for details.");
            uploadLog.setStatus(UploadStatus.FAILED);
            uploadLog.setErrorMessage("Excel contains invalid rows.");
            uploadLogRepository.save(uploadLog);
            return result;
        }

        // 所有行合法，保存为 Task
        List<Task> tasksToSave = new ArrayList<>();
        for (ExcelTaskData data : rows) {
            Task task = mapToTask(data);
            tasksToSave.add(task);
        }
        taskRepository.saveAll(tasksToSave);

        uploadLog.setStatus(UploadStatus.SUCCESS);
        uploadLogRepository.save(uploadLog);

        result.setTasks(rows);
        result.setSuccess(true);
        return result;
    }

    private ExcelTaskData parseRow(int displayRowIndex, Row row) {
        ExcelTaskData data = new ExcelTaskData();

        String rundownCode = getStringCellValue(row.getCell(COL_RUNDOWN_CODE));
        String rundownName = getStringCellValue(row.getCell(COL_RUNDOWN_NAME));
        String seqStr = getStringCellValue(row.getCell(COL_SEQUENCE));
        String taskName = getStringCellValue(row.getCell(COL_TASK_NAME));
        String description = getStringCellValue(row.getCell(COL_DESCRIPTION));
        String automationTypeStr = getStringCellValue(row.getCell(COL_AUTOMATION_TYPE));
        String automationTarget = getStringCellValue(row.getCell(COL_AUTOMATION_TARGET));
        String automationParams = getStringCellValue(row.getCell(COL_AUTOMATION_PARAMS));
        String scheduleMode = getStringCellValue(row.getCell(COL_SCHEDULE_MODE));
        String scheduledTimeStr = getStringCellValue(row.getCell(COL_SCHEDULED_TIME));
        String cronExpression = getStringCellValue(row.getCell(COL_CRON_EXPRESSION));

        data.setRundownCode(rundownCode);
        data.setRundownName(rundownName);
        data.setName(taskName);
        data.setDescription(description);
        data.setAutomationTarget(automationTarget);
        data.setAutomationParamsJson(automationParams);
        data.setScheduleMode(scheduleMode);
        data.setCronExpression(cronExpression);

        if (rundownCode == null || rundownCode.isBlank()) {
            data.addError("Row " + displayRowIndex + ": Rundown Code is required.");
        }
        if (taskName == null || taskName.isBlank()) {
            data.addError("Row " + displayRowIndex + ": Task Name is required.");
        }
        if (seqStr == null || seqStr.isBlank()) {
            data.addError("Row " + displayRowIndex + ": Sequence is required.");
        } else {
            try {
                data.setSequenceNo(Integer.parseInt(seqStr));
            } catch (NumberFormatException e) {
                data.addError("Row " + displayRowIndex + ": Sequence must be an integer.");
            }
        }

        if (automationTypeStr == null || automationTypeStr.isBlank()) {
            data.addError("Row " + displayRowIndex + ": Automation Type is required (JENKINS/ANSIBLE).");
        } else {
            try {
                data.setAutomationType(TaskType.valueOf(automationTypeStr.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                data.addError("Row " + displayRowIndex + ": Invalid Automation Type, expected JENKINS or ANSIBLE.");
            }
        }

        if (scheduleMode == null || scheduleMode.isBlank()) {
            data.addError("Row " + displayRowIndex + ": Schedule Mode is required (IMMEDIATE/ONCE/CRON).");
        } else {
            String mode = scheduleMode.trim().toUpperCase();
            data.setScheduleMode(mode);
            switch (mode) {
                case "IMMEDIATE":
                    // no extra fields required
                    break;
                case "ONCE":
                    if (scheduledTimeStr == null || scheduledTimeStr.isBlank()) {
                        data.addError("Row " + displayRowIndex + ": Scheduled Time is required for ONCE mode.");
                    } else {
                        try {
                            LocalDateTime ldt = LocalDateTime.parse(scheduledTimeStr.trim(), DATE_TIME_FORMATTER);
                            Instant instant = ldt.atZone(ZoneId.systemDefault()).toInstant();
                            data.setScheduledTime(instant);
                        } catch (Exception e) {
                            data.addError("Row " + displayRowIndex + ": Scheduled Time format must be yyyy-MM-dd HH:mm:ss.");
                        }
                    }
                    break;
                case "CRON":
                    if (cronExpression == null || cronExpression.isBlank()) {
                        data.addError("Row " + displayRowIndex + ": Cron Expression is required for CRON mode.");
                    }
                    break;
                default:
                    data.addError("Row " + displayRowIndex + ": Invalid Schedule Mode, expected IMMEDIATE/ONCE/CRON.");
            }
        }

        return data;
    }

    private Task mapToTask(ExcelTaskData data) {
        Task task = new Task();
        task.setRundownCode(data.getRundownCode());
        task.setRundownName(data.getRundownName());
        task.setSequenceNo(data.getSequenceNo());
        task.setName(data.getName());
        task.setDescription(data.getDescription());
        task.setAutomationType(data.getAutomationType());
        task.setAutomationTarget(data.getAutomationTarget());
        task.setAutomationParamsJson(data.getAutomationParamsJson());

        // 默认状态：未执行
        task.setStatus(TaskStatus.PENDING);

        String mode = data.getScheduleMode() != null ? data.getScheduleMode().toUpperCase() : "IMMEDIATE";
        switch (mode) {
            case "IMMEDIATE":
                task.setSchedulingEnabled(false);
                task.setScheduledTime(null);
                task.setCronExpression(null);
                task.setNextRunAt(null);
                break;
            case "ONCE":
                task.setSchedulingEnabled(true);
                task.setScheduledTime(data.getScheduledTime());
                task.setCronExpression(null);
                task.setNextRunAt(data.getScheduledTime());
                break;
            case "CRON":
                task.setSchedulingEnabled(true);
                task.setCronExpression(data.getCronExpression());
                task.setScheduledTime(null);
                task.setNextRunAt(null);
                break;
            default:
                task.setSchedulingEnabled(false);
        }

        task.setLastScheduledRunAt(null);
        task.setStartedAt(null);
        task.setFinishedAt(null);

        return task;
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }

    /**
     * Check if the file has a valid extension (.xlsx or .xls)
     */
    private boolean isValidExtension(String filename) {
        String lowerCaseFilename = filename.toLowerCase();
        for (String ext : ALLOWED_EXTENSIONS) {
            if (lowerCaseFilename.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the file has a valid MIME type
     */
    private boolean isValidContentType(String contentType) {
        if (contentType == null) {
            return false;
        }
        for (String allowedType : ALLOWED_CONTENT_TYPES) {
            if (allowedType.equals(contentType)) {
                return true;
            }
        }
        return false;
    }
}

