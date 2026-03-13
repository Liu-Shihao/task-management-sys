package com.taskmanagement.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

/**
 * Excel file parser
 */
@Slf4j
@Component
public class ExcelParser {

    private static final String[] REQUIRED_FIELDS = {"name", "taskType", "orderIndex"};
    private static final Set<String> VALID_TASK_TYPES = Set.of("jenkins", "ansible");

    /**
     * Parse Excel file to list of maps
     */
    public List<Map<String, Object>> parse(MultipartFile file) {
        log.info("Parsing Excel file: {}", file.getOriginalFilename());

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            
            if (headerRow == null) {
                throw new IllegalArgumentException("Empty Excel file");
            }

            // Get headers
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(getCellValue(cell).toString().trim().toLowerCase());
            }

            // Parse rows
            List<Map<String, Object>> data = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, Object> rowData = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    Object value = cell != null ? getCellValue(cell) : null;
                    rowData.put(headers.get(j), value);
                }
                
                // Skip empty rows
                if (!rowData.isEmpty() && rowData.values().stream().anyMatch(Objects::nonNull)) {
                    data.add(rowData);
                }
            }

            log.info("Parsed {} rows from Excel", data.size());
            return data;

        } catch (Exception e) {
            log.error("Error parsing Excel file", e);
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage(), e);
        }
    }

    /**
     * Validate Excel file format
     */
    public boolean validateFormat(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return false;
        }
        
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return ext.equals("xlsx") || ext.equals("xls");
    }

    /**
     * Validate parsed data
     */
    public ValidationResult validate(List<Map<String, Object>> data) {
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> row = data.get(i);
            int rowNum = i + 2; // +2 because row 0 is header, row 1 is first data

            // Check required fields
            for (String field : REQUIRED_FIELDS) {
                if (!row.containsKey(field) || row.get(field) == null) {
                    errors.add("Row " + rowNum + ": Missing required field '" + field + "'");
                }
            }

            // Validate task type
            if (row.containsKey("taskType")) {
                String taskType = String.valueOf(row.get("taskType")).toLowerCase();
                if (!VALID_TASK_TYPES.contains(taskType)) {
                    errors.add("Row " + rowNum + ": Invalid taskType '" + taskType + 
                             "'. Must be one of: " + VALID_TASK_TYPES);
                }
            }

            // Validate orderIndex
            if (row.containsKey("orderIndex")) {
                try {
                    Integer.parseInt(String.valueOf(row.get("orderIndex")));
                } catch (NumberFormatException e) {
                    errors.add("Row " + rowNum + ": Invalid orderIndex '" + row.get("orderIndex") + "'");
                }
            }
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    private Object getCellValue(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell) 
                    ? cell.getDateCellValue() 
                    : cell.getNumericCellValue();
            case BOOLEAN -> cell.getBooleanCellValue();
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    /**
     * Validation result
     */
    public record ValidationResult(boolean valid, List<String> errors) {}
}
