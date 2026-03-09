package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.resp.ActivityLogResponseDTO;
import com.ra.base_spring_boot.model.constants.ActivityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IActivityLogQueryService {
    Page<ActivityLogResponseDTO> getAllActivityLogs(Pageable pageable);
    List<ActivityLogResponseDTO> getRecentActivityLogs(int limit);
    List<ActivityLogResponseDTO> getActivityLogsByUser(Long userId);
    List<ActivityLogResponseDTO> getActivityLogsByType(ActivityType activityType);
}

