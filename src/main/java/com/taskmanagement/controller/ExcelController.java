package com.taskmanagement.controller;

import com.taskmanagement.dto.ExcelUploadResult;
import com.taskmanagement.service.ExcelService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/excel")
public class ExcelController {

    private final ExcelService excelService;

    public ExcelController(ExcelService excelService) {
        this.excelService = excelService;
    }

    /**
     * User Story 1: Excel File Upload
     *
     * 支持上传 .xlsx / .xls 文件，解析为 Rundown Task，并区分：
     * - IMMEDIATE：立即执行（仅保存为待执行状态，不启用调度）
     * - ONCE：单次调度执行
     * - CRON：周期调度执行
     */
    @PostMapping(
            path = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ExcelUploadResult> uploadExcel(@RequestParam("file") MultipartFile file) {
        ExcelUploadResult result = excelService.uploadAndParse(file);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }
}

