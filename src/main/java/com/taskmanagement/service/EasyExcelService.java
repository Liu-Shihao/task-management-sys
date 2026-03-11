package com.taskmanagement.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.taskmanagement.dto.ExcelTaskData;
import com.taskmanagement.dto.ExcelUploadResult;
import com.taskmanagement.dto.TemplateTaskDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class EasyExcelService {

    public ExcelUploadResult parseExcelFile(MultipartFile file) {
        if (file.isEmpty()) {
            return ExcelUploadResult.error("File is empty", List.of("Please upload a non-empty file"));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return ExcelUploadResult.error("Invalid file type", 
                List.of("Only .xlsx and .xls files are supported"));
        }

        ExcelDataListener listener = new ExcelDataListener();
        
        try {
            EasyExcel.read(file.getInputStream(), ExcelTaskData.class, listener)
                    .sheet()
                    .doRead();

            if (!listener.getErrors().isEmpty()) {
                return ExcelUploadResult.error("Validation failed", listener.getErrors());
            }

            if (listener.getTasks().isEmpty()) {
                return ExcelUploadResult.error("No data", List.of("No valid data rows found in the file"));
            }

            log.info("[EasyExcel] Successfully parsed {} tasks", listener.getTasks().size());
            return ExcelUploadResult.success(
                String.format("Successfully parsed %d tasks (EasyExcel)", listener.getTasks().size()),
                listener.getTasks(),
                listener.getTasks().size()
            );

        } catch (ExcelDataConvertException e) {
            log.error("[EasyExcel] Data conversion error", e);
            return ExcelUploadResult.error("Data conversion error", 
                List.of("Row " + e.getRowIndex() + ", Column " + e.getColumnIndex() + ": " + e.getMessage()));
        } catch (IOException e) {
            log.error("[EasyExcel] Error reading file", e);
            return ExcelUploadResult.error("Error reading file", 
                List.of("Failed to read the Excel file: " + e.getMessage()));
        } catch (Exception e) {
            log.error("[EasyExcel] Parse error", e);
            return ExcelUploadResult.error("Parse error", 
                List.of("Error parsing Excel file: " + e.getMessage()));
        }
    }

    public static class ExcelDataListener extends AnalysisEventListener<ExcelTaskData> {
        
        private final List<TemplateTaskDTO> tasks = new ArrayList<>();
        private final List<String> errors = new ArrayList<>();
        private int rowIndex = 1;

        @Override
        public void invoke(ExcelTaskData data, AnalysisContext context) {
            List<String> rowErrors = validateRow(data, rowIndex);
            
            if (rowErrors.isEmpty()) {
                tasks.add(data.toTemplateTaskDTO());
            } else {
                errors.addAll(rowErrors);
            }
            rowIndex++;
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            log.info("[EasyExcel] Finished reading {} rows, {} valid tasks", rowIndex - 1, tasks.size());
        }

        @Override
        public void onException(Exception exception, AnalysisContext context) {
            log.error("[EasyExcel] Error at row {}", context.readRowHolder().getRowIndex(), exception);
            errors.add("Row " + context.readRowHolder().getRowIndex() + ": " + exception.getMessage());
        }

        private List<String> validateRow(ExcelTaskData data, int rowNum) {
            List<String> rowErrors = new ArrayList<>();
            
            if (data.getTaskName() == null || data.getTaskName().trim().isEmpty()) {
                rowErrors.add("Row " + rowNum + ": Task Name is required");
            }
            
            if (data.getTaskType() == null || data.getTaskType().trim().isEmpty()) {
                rowErrors.add("Row " + rowNum + ": Task Type is required");
            } else {
                String type = data.getTaskType().toUpperCase().trim();
                if (!type.equals("JENKINS") && !type.equals("ANSIBLE") && !type.equals("MANUAL")) {
                    rowErrors.add("Row " + rowNum + ": Invalid task type '" + data.getTaskType() + "'. Must be JENKINS, ANSIBLE, or MANUAL");
                }
            }
            
            if (data.getSequenceOrder() == null) {
                rowErrors.add("Row " + rowNum + ": Sequence Order is required");
            }
            
            return rowErrors;
        }

        public List<TemplateTaskDTO> getTasks() { return tasks; }
        public List<String> getErrors() { return errors; }
    }
}
