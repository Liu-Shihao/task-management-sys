package com.taskmanagement.controller;

import com.taskmanagement.dto.ExcelUploadResult;
import com.taskmanagement.service.EasyExcelService;
import com.taskmanagement.service.ExcelService;
import com.taskmanagement.service.UploadLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/excel")
@RequiredArgsConstructor
public class ExcelController {

    private final ExcelService excelService;
    private final EasyExcelService easyExcelService;
    private final UploadLogService uploadLogService;

    /**
     * 使用 Apache POI 解析 Excel
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "staffId", required = false) String staffId) {
        log.info("[POI] Received Excel upload request: {}, size: {} bytes, staffId: {}", 
                file.getOriginalFilename(), file.getSize(), staffId);
        
        ExcelUploadResult result = excelService.parseExcelFile(file, staffId);
        
        // 记录上传日志
        uploadLogService.logUpload(file, "POI", result);
        
        return buildResponse(result);
    }

    /**
     * 使用 Alibaba EasyExcel 解析 Excel
     */
    @PostMapping("/upload/easyexcel")
    public ResponseEntity<Map<String, Object>> uploadExcelEasyExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "staffId", required = false) String staffId) {
        log.info("[EasyExcel] Received Excel upload request: {}, size: {} bytes, staffId: {}", 
                file.getOriginalFilename(), file.getSize(), staffId);
        
        ExcelUploadResult result = easyExcelService.parseExcelFile(file, staffId);
        
        // 记录上传日志
        uploadLogService.logUpload(file, "EasyExcel", result);
        
        return buildResponse(result);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(ExcelUploadResult result) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isSuccess());
        response.put("message", result.getMessage());
        
        if (result.isSuccess()) {
            response.put("rowCount", result.getRowCount());
            response.put("staffId", result.getUsername());
            response.put("tasks", result.getTasks());
        } else {
            response.put("errors", result.getErrors());
        }
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
