package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.model.constants.ActivityType;
import com.ra.base_spring_boot.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;

public interface IActivityLogService {
    void logActivity(User user, ActivityType activityType, String description, HttpServletRequest request);
    void logActivity(User user, ActivityType activityType, String description, String ipAddress, String userAgent, String requestUrl, String requestMethod);
    void logActivity(User user, ActivityType activityType, String description, String oldValue, String newValue, HttpServletRequest request);
}

