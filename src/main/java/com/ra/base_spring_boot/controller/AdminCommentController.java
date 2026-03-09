package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.ResponseWrapper;
import com.ra.base_spring_boot.dto.req.CommentAdvancedFilterDTO;
import com.ra.base_spring_boot.dto.req.CommentToggleHiddenRequestDTO;
import com.ra.base_spring_boot.dto.resp.CommentResponseDTO;
import com.ra.base_spring_boot.services.ICommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {
    
    private final ICommentService commentService;
    
    /**
     * Advanced filter for comments (by user, by keyword, by date)
     * Only accessible by ROLE_ADMIN or ROLE_WRITER (article owner)
     */
    @GetMapping("/advanced-filter")
    public ResponseEntity<ResponseWrapper<Page<CommentResponseDTO>>> advancedFilter(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long articleId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        String username = getCurrentUsername();
        
        CommentAdvancedFilterDTO dto = new CommentAdvancedFilterDTO();
        dto.setUserId(userId);
        dto.setKeyword(keyword);
        dto.setArticleId(articleId);
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CommentResponseDTO> result = commentService.advancedFilter(dto, username, pageable);
        
        return ResponseEntity.ok(ResponseWrapper.<Page<CommentResponseDTO>>builder()
                .status(HttpStatus.OK)
                .code(200)
                .data(result)
                .build());
    }

    @PatchMapping("/{id}/toggle-hidden")
    public ResponseEntity<ResponseWrapper<CommentResponseDTO>> toggleHidden(
            @PathVariable Long id,
            @RequestBody(required = false) CommentToggleHiddenRequestDTO request) {
        String username = getCurrentUsername();
        String reason = request != null ? request.getReason() : null;
        CommentResponseDTO result = commentService.toggleHidden(id, reason, username);
        return ResponseEntity.ok(ResponseWrapper.<CommentResponseDTO>builder()
                .status(HttpStatus.OK)
                .code(200)
                .data(result)
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Void>> delete(@PathVariable Long id) {
        commentService.delete(id);
        return ResponseEntity.ok(ResponseWrapper.<Void>builder()
                .status(HttpStatus.OK)
                .code(200)
                .message("Comment deleted successfully")
                .build());
    }
    
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}

