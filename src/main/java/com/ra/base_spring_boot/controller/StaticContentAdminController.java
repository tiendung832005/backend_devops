package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.ResponseWrapper;
import com.ra.base_spring_boot.dto.req.StaticContentRequest;
import com.ra.base_spring_boot.security.principle.MyUserDetails;
import com.ra.base_spring_boot.services.StaticContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/admin/static-contents")
@RequiredArgsConstructor
public class StaticContentAdminController {

    private final StaticContentService service;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        return userDetails.getUser().getId();
    }

    @GetMapping
    public ResponseWrapper<?> getAll() {
        return ResponseWrapper.builder()
                .code(200)
                .status(HttpStatus.OK)
                .message("Get all static contents success")
                .data(service.getAll())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping("/{id}")
    public ResponseWrapper<?> getById(@PathVariable Long id) {
        return ResponseWrapper.builder()
                .code(200)
                .status(HttpStatus.OK)
                .message("Get static content success")
                .data(service.getById(id))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @PostMapping
    public ResponseWrapper<?> create(
            @Valid @RequestBody StaticContentRequest request
    ) {
        Long userId = getCurrentUserId();
        return ResponseWrapper.builder()
                .code(201)
                .status(HttpStatus.CREATED)
                .message("Create static content success")
                .data(service.create(request, userId))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @PutMapping("/{id}")
    public ResponseWrapper<?> update(
            @PathVariable Long id,
            @Valid @RequestBody StaticContentRequest request
    ) {
        Long userId = getCurrentUserId();
        return ResponseWrapper.builder()
                .code(200)
                .status(HttpStatus.OK)
                .message("Update static content success")
                .data(service.update(id, request, userId))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseWrapper<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseWrapper.builder()
                .code(200)
                .status(HttpStatus.OK)
                .message("Delete static content success")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
