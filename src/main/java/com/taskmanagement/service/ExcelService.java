package com.taskmanagement.service;

import com.taskmanagement.dto.ExcelUploadResult;
import org.springframework.web.multipart.MultipartFile;

public interface ExcelService {

    /**
     * 解析上传的 Excel，并将合法的任务保存为 Task 实体。
     * 如果存在格式错误，会在结果中返回错误信息，并在失败时不落库任何 Task。
     */
    ExcelUploadResult uploadAndParse(MultipartFile file);
}

