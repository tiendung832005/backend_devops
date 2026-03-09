package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.controller.ActivityLogController;
import com.ra.base_spring_boot.model.constants.ActivityType;
import com.ra.base_spring_boot.model.entity.User;
import com.ra.base_spring_boot.model.entity.UserActivityLog;
import com.ra.base_spring_boot.repository.IUserActivityLogRepository;
import com.ra.base_spring_boot.services.IActivityLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityLogServiceImpl implements IActivityLogService {

    private final IUserActivityLogRepository activityLogRepository;
    private final ApplicationContext applicationContext;

    @Override
    @Transactional
    public void logActivity(User user, ActivityType activityType, String description, HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String requestUrl = request.getRequestURI();
        String requestMethod = request.getMethod();
        
        logActivity(user, activityType, description, ipAddress, userAgent, requestUrl, requestMethod);
    }

    @Override
    @Transactional
    public void logActivity(User user, ActivityType activityType, String description, String ipAddress, String userAgent, String requestUrl, String requestMethod) {
        try {
            UserActivityLog activityLog = UserActivityLog.builder()
                    .user(user)
                    .activityType(activityType)
                    .actionDescription(description)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .requestUrl(requestUrl)
                    .requestMethod(requestMethod)
                    .status("SUCCESS")
                    .build();

            UserActivityLog savedLog = activityLogRepository.save(activityLog);
            
            // Broadcast to SSE clients (async to avoid blocking)
            try {
                ActivityLogController controller = applicationContext.getBean(ActivityLogController.class);
                controller.broadcastActivityLog(savedLog);
            } catch (Exception e) {
                log.warn("Could not broadcast activity log to SSE clients: {}", e.getMessage());
            }
        } catch (Exception e) {
            log.error("Error logging activity for user {}: {}", user != null ? user.getUsername() : "unknown", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void logActivity(User user, ActivityType activityType, String description, String oldValue, String newValue, HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String requestUrl = request.getRequestURI();
        String requestMethod = request.getMethod();
        
        String fullDescription = description;
        if (oldValue != null || newValue != null) {
            fullDescription += String.format(" (Old: %s, New: %s)", oldValue, newValue);
        }
        
        logActivity(user, activityType, fullDescription, ipAddress, userAgent, requestUrl, requestMethod);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        // If multiple IPs, take the first one
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }

    public HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}

