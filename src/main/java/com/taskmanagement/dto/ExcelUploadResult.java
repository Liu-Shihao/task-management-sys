package com.taskmanagement.dto;

import java.util.ArrayList;
import java.util.List;

public class ExcelUploadResult {

    private boolean success;
    private List<ExcelTaskData> tasks = new ArrayList<>();
    private List<String> errors = new ArrayList<>();

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<ExcelTaskData> getTasks() {
        return tasks;
    }

    public void setTasks(List<ExcelTaskData> tasks) {
        this.tasks = tasks;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public void addError(String error) {
        this.errors.add(error);
    }
}

