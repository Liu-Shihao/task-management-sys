package com.taskmanagement.service;

import com.taskmanagement.dto.ExcelTaskData;
import com.taskmanagement.dto.ExcelUploadResult;
import com.taskmanagement.dto.TemplateTaskDTO;
import com.taskmanagement.enums.TaskType;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class ExcelService {

    private static final String[] REQUIRED_HEADERS = {"Task Name", "Task Type", "Sequence Order"};
    private static final List<String> VALID_TASK_TYPES = Arrays.asList("JENKINS", "ANSIBLE", "MANUAL");

    public ExcelUploadResult parseExcelFile(MultipartFile file, String staffId) {
        if (file.isEmpty()) {
            return ExcelUploadResult.error("File is empty", List.of("Please upload a non-empty file"), staffId);
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return ExcelUploadResult.error("Invalid file type", 
                List.of("Only .xlsx and .xls files are supported"), staffId);
        }

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return ExcelUploadResult.error("Empty workbook", List.of("No sheet found in the workbook"), staffId);
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return ExcelUploadResult.error("Invalid format", List.of("Header row is missing"), staffId);
            }

            List<String> errors = validateHeaders(headerRow);
            if (!errors.isEmpty()) {
                return ExcelUploadResult.error("Invalid headers", errors, staffId);
            }

            List<TemplateTaskDTO> tasks = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (isRowEmpty(row)) {
                    continue;
                }

                List<String> rowErrors = new ArrayList<>();
                TemplateTaskDTO taskDTO = parseRow(row, i, rowErrors);
                
                if (!rowErrors.isEmpty()) {
                    errors.addAll(rowErrors);
                } else {
                    tasks.add(taskDTO);
                }
            }

            if (!errors.isEmpty()) {
                return ExcelUploadResult.error("Validation failed", errors, staffId);
            }

            if (tasks.isEmpty()) {
                return ExcelUploadResult.error("No data", List.of("No valid data rows found in the file"), staffId);
            }

            log.info("[POI] Successfully parsed {} tasks from Excel file, uploaded by: {}", tasks.size(), staffId);
            return ExcelUploadResult.success(
                String.format("Successfully parsed %d tasks", tasks.size()),
                tasks,
                tasks.size(),
                staffId
            );

        } catch (IOException e) {
            log.error("[POI] Error reading Excel file", e);
            return ExcelUploadResult.error("Error reading file", 
                List.of("Failed to read the Excel file: " + e.getMessage()), staffId);
        } catch (Exception e) {
            log.error("[POI] Error parsing Excel file", e);
            return ExcelUploadResult.error("Parse error", 
                List.of("Error parsing Excel file: " + e.getMessage()), staffId);
        }
    }

    private List<String> validateHeaders(Row headerRow) {
        List<String> errors = new ArrayList<>();
        
        for (int i = 0; i < REQUIRED_HEADERS.length; i++) {
            Cell cell = headerRow.getCell(i);
            String headerValue = getCellStringValue(cell);
            
            if (headerValue == null || !headerValue.equalsIgnoreCase(REQUIRED_HEADERS[i])) {
                errors.add(String.format("Column %d should be '%s' but found '%s'", 
                    i + 1, REQUIRED_HEADERS[i], headerValue));
            }
        }
        
        return errors;
    }

    private TemplateTaskDTO parseRow(Row row, int rowNum, List<String> errors) {
        TemplateTaskDTO dto = new TemplateTaskDTO();

        Cell nameCell = row.getCell(0);
        String taskName = getCellStringValue(nameCell);
        if (taskName == null || taskName.trim().isEmpty()) {
            errors.add(String.format("Row %d: Task Name is required", rowNum + 1));
        }
        dto.setTaskName(taskName);

        Cell typeCell = row.getCell(1);
        String taskTypeStr = getCellStringValue(typeCell);
        if (taskTypeStr == null || taskTypeStr.trim().isEmpty()) {
            errors.add(String.format("Row %d: Task Type is required", rowNum + 1));
        } else {
            try {
                TaskType taskType = TaskType.valueOf(taskTypeStr.toUpperCase().trim());
                dto.setTaskType(taskType);
            } catch (IllegalArgumentException e) {
                errors.add(String.format("Row %d: Invalid task type '%s'. Must be JENKINS, ANSIBLE, or MANUAL", 
                    rowNum + 1, taskTypeStr));
                dto.setTaskType(TaskType.MANUAL);
            }
        }

        Cell orderCell = row.getCell(2);
        Integer sequenceOrder = getCellIntegerValue(orderCell);
        if (sequenceOrder == null) {
            errors.add(String.format("Row %d: Sequence Order is required and must be a number", rowNum + 1));
            dto.setSequenceOrder(rowNum);
        } else {
            dto.setSequenceOrder(sequenceOrder);
        }

        Cell configCell = row.getCell(3);
        dto.setConfigJson(getCellStringValue(configCell));

        return dto;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int i = 0; i < 3; i++) {
            Cell cell = row.getCell(i);
            String value = getCellStringValue(cell);
            if (value != null && !value.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                }
                yield String.valueOf((int) cell.getNumericCellValue());
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue();
                } catch (Exception e) {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            default -> null;
        };
    }

    private Integer getCellIntegerValue(Cell cell) {
        if (cell == null) return null;
        
        return switch (cell.getCellType()) {
            case NUMERIC -> (int) cell.getNumericCellValue();
            case STRING -> {
                try {
                    yield Integer.parseInt(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
            case FORMULA -> {
                try {
                    yield (int) cell.getNumericCellValue();
                } catch (Exception e) {
                    yield null;
                }
            }
            default -> null;
        };
    }
}
