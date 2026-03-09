package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.ResponseWrapper;
import com.ra.base_spring_boot.dto.resp.ActivityLogResponseDTO;
import com.ra.base_spring_boot.model.constants.ActivityType;
import com.ra.base_spring_boot.model.entity.UserActivityLog;
import com.ra.base_spring_boot.services.IActivityLogQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/v1/activity-logs")
@RequiredArgsConstructor
@Slf4j
public class ActivityLogController {

    private final IActivityLogQueryService activityLogQueryService;
    
    // Store active SSE emitters for real-time updates
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @GetMapping
    public ResponseEntity<?> getAllActivityLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ActivityLogResponseDTO> logs = activityLogQueryService.getAllActivityLogs(pageable);
        return ResponseEntity.ok(ResponseWrapper.builder()
                .status(HttpStatus.OK)
                .code(200)
                .data(logs)
                .build());
    }

    @GetMapping("/recent")
    public ResponseEntity<?> getRecentActivityLogs(@RequestParam(defaultValue = "50") int limit) {
        List<ActivityLogResponseDTO> logs = activityLogQueryService.getRecentActivityLogs(limit);
        return ResponseEntity.ok(ResponseWrapper.builder()
                .status(HttpStatus.OK)
                .code(200)
                .data(logs)
                .build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getActivityLogsByUser(@PathVariable Long userId) {
        List<ActivityLogResponseDTO> logs = activityLogQueryService.getActivityLogsByUser(userId);
        return ResponseEntity.ok(ResponseWrapper.builder()
                .status(HttpStatus.OK)
                .code(200)
                .data(logs)
                .build());
    }

    @GetMapping("/type/{activityType}")
    public ResponseEntity<?> getActivityLogsByType(@PathVariable ActivityType activityType) {
        List<ActivityLogResponseDTO> logs = activityLogQueryService.getActivityLogsByType(activityType);
        return ResponseEntity.ok(ResponseWrapper.builder()
                .status(HttpStatus.OK)
                .code(200)
                .data(logs)
                .build());
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamActivityLogs() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        emitter.onCompletion(() -> {
            log.info("SSE connection completed");
            emitters.remove(emitter);
        });
        
        emitter.onTimeout(() -> {
            log.info("SSE connection timeout");
            emitters.remove(emitter);
        });
        
        emitter.onError((ex) -> {
            log.error("SSE connection error: {}", ex.getMessage());
            emitters.remove(emitter);
        });
        
        emitters.add(emitter);
        
        // Send initial connection message
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("Connected to activity log stream"));
        } catch (IOException e) {
            log.error("Error sending initial SSE message", e);
            emitters.remove(emitter);
        }
        
        log.info("New SSE connection established. Total connections: {}", emitters.size());
        
        return emitter;
    }

    // Method to broadcast new activity log to all connected clients
    public void broadcastActivityLog(UserActivityLog activityLog) {
        ActivityLogResponseDTO dto = new ActivityLogResponseDTO(activityLog);
        
        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
        
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("activity")
                        .data(dto));
            } catch (IOException e) {
                log.error("Error sending SSE message to client", e);
                deadEmitters.add(emitter);
            }
        });
        
        emitters.removeAll(deadEmitters);
    }
}