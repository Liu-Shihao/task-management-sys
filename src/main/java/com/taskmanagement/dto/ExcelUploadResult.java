package com.taskmanagement.dto;

import com.taskmanagement.enums.TaskType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelUploadResult {

    private boolean success;
    private String message;
    private int rowCount;
    private String staffId;  // 上传用户
    private List<TemplateTaskDTO> tasks;
    private List<String> errors;

    public static ExcelUploadResult success(String message, List<TemplateTaskDTO> tasks, int rowCount, String staffId) {
        return ExcelUploadResult.builder()
                .success(true)
                .message(message)
                .tasks(tasks)
                .rowCount(rowCount)
                .staffId(staffId)
                .build();
    }

    public static ExcelUploadResult error(String message, List<String> errors, String staffId) {
        return ExcelUploadResult.builder()
                .success(false)
                .message(message)
                .errors(errors)
                .staffId(staffId)
                .build();
    }
}
