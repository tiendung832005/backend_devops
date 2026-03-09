package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.ResponseWrapper;
import com.ra.base_spring_boot.dto.req.CommentRequestDTO;
import com.ra.base_spring_boot.dto.req.CommentSearchDTO;
import com.ra.base_spring_boot.dto.resp.ArticlesResponseDTO;
import com.ra.base_spring_boot.dto.resp.CommentResponseDTO;
import com.ra.base_spring_boot.services.ICommentReportService;
import com.ra.base_spring_boot.services.ICommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user/comments")
@RequiredArgsConstructor
public class CommentController
{
    private final ICommentService commentService;

    @PostMapping
    public ResponseEntity<ResponseWrapper<CommentResponseDTO>> create(@Valid @RequestBody CommentRequestDTO dto)
    {
        String username = getCurrentUsername();
        CommentResponseDTO response = commentService.create(dto, username);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseWrapper.<CommentResponseDTO>builder()
                        .status(HttpStatus.CREATED)
                        .code(201)
                        .data(response)
                        .build());
    }

    @GetMapping("/article/{articleId}")
    public ResponseEntity<ResponseWrapper<List<CommentResponseDTO>>> getByArticle(@PathVariable Long articleId)
    {
        List<CommentResponseDTO> comments = commentService.getByArticle(articleId);
        return ResponseEntity.ok(ResponseWrapper.<List<CommentResponseDTO>>builder()
                .status(HttpStatus.OK)
                .code(200)
                .data(comments)
                .build());
    }

    @GetMapping("/commented-articles")
    public ResponseEntity<ResponseWrapper<List<ArticlesResponseDTO>>> getCommentedArticles()
    {
        String username = getCurrentUsername();
        List<ArticlesResponseDTO> articles = commentService.getCommentedArticles(username);
        return ResponseEntity.ok(ResponseWrapper.<List<ArticlesResponseDTO>>builder()
                .status(HttpStatus.OK)
                .code(200)
                .data(articles)
                .build());
    }

    @GetMapping("/search")
    public ResponseEntity<ResponseWrapper<Page<CommentResponseDTO>>> searchComments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long articleId,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    )
    {
        CommentSearchDTO dto = new CommentSearchDTO();
        dto.setKeyword(keyword);
        dto.setArticleId(articleId);
        dto.setUserId(userId);

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CommentResponseDTO> result = commentService.search(dto, pageable);

        return ResponseEntity.ok(ResponseWrapper.<Page<CommentResponseDTO>>builder()
                .status(HttpStatus.OK)
                .code(200)
                .data(result)
                .build());
    }

    private String getCurrentUsername()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}

