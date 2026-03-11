package com.taskmanagement.service;

import com.taskmanagement.dto.ExcelUploadResult;
import com.taskmanagement.entity.UploadLog;
import com.taskmanagement.repository.UploadLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadLogService {

    private final UploadLogRepository uploadLogRepository;

    /**
     * 记录 Excel 上传日志
     */
    public void logUpload(MultipartFile file, String parserType, ExcelUploadResult result) {
        try {
            HttpServletRequest request = getCurrentRequest();
            
            UploadLog uploadLog = UploadLog.builder()
                    .fileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .uploadedBy(getCurrentUser())
                    .parserType(parserType)
                    .success(result.isSuccess())
                    .rowCount(result.getRowCount())
                    .errorMessage(result.isSuccess() ? null : String.join("; ", result.getErrors()))
                    .clientIp(getClientIp(request))
                    .userAgent(getUserAgent(request))
                    .build();
            
            uploadLogRepository.save(uploadLog);
            log.info("[UploadLog] Saved upload log: file={}, parser={}, success={}, user={}", 
                    file.getOriginalFilename(), parserType, result.isSuccess(), uploadLog.getUploadedBy());
                    
        } catch (Exception e) {
            log.error("[UploadLog] Failed to save upload log", e);
        }
    }

    private HttpServletRequest getCurrentRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(attrs -> attrs instanceof ServletRequestAttributes)
                .map(attrs -> ((ServletRequestAttributes) attrs).getRequest())
                .orElse(null);
    }

    private String getCurrentUser() {
        // TODO: 集成 Spring Security 后从 SecurityContext 获取
        return "anonymous";
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) return null;
        
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private String getUserAgent(HttpServletRequest request) {
        return request != null ? request.getHeader("User-Agent") : null;
    }
}
