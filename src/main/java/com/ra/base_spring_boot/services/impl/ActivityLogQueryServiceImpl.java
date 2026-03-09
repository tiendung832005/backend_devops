package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.resp.ActivityLogResponseDTO;
import com.ra.base_spring_boot.model.constants.ActivityType;
import com.ra.base_spring_boot.repository.IUserActivityLogRepository;
import com.ra.base_spring_boot.services.IActivityLogQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityLogQueryServiceImpl implements IActivityLogQueryService {

    private final IUserActivityLogRepository activityLogRepository;

    @Override
    public Page<ActivityLogResponseDTO> getAllActivityLogs(Pageable pageable) {
        return activityLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(ActivityLogResponseDTO::new);
    }

    @Override
    public List<ActivityLogResponseDTO> getRecentActivityLogs(int limit) {
        LocalDateTime since = LocalDateTime.now().minusHours(24); // Last 24 hours
        return activityLogRepository.findRecentActivities(since).stream()
                .limit(limit)
                .map(ActivityLogResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<ActivityLogResponseDTO> getActivityLogsByUser(Long userId) {
        return activityLogRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(ActivityLogResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<ActivityLogResponseDTO> getActivityLogsByType(ActivityType activityType) {
        return activityLogRepository.findByActivityTypeOrderByCreatedAtDesc(activityType).stream()
                .map(ActivityLogResponseDTO::new)
                .collect(Collectors.toList());
    }
}

