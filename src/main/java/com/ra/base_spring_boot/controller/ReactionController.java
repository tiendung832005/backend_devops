package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.ResponseWrapper;
import com.ra.base_spring_boot.dto.req.ReactionRequestDTO;
import com.ra.base_spring_boot.dto.resp.ArticlesResponseDTO;
import com.ra.base_spring_boot.dto.resp.ReactionResponseDTO;
import com.ra.base_spring_boot.services.IReactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user/reactions")
@RequiredArgsConstructor
public class ReactionController
{
    private final IReactionService reactionService;

    @PostMapping
    public ResponseEntity<ResponseWrapper<ReactionResponseDTO>> react(@Valid @RequestBody ReactionRequestDTO dto)
    {
        String username = getCurrentUsername();
        ReactionResponseDTO response = reactionService.react(dto, username);
        return ResponseEntity.ok(ResponseWrapper.<ReactionResponseDTO>builder()
                .status(HttpStatus.OK)
                .code(200)
                .data(response)
                .build());
    }

    @GetMapping("/{articleId}")
    public ResponseEntity<ResponseWrapper<ReactionResponseDTO>> getReactions(@PathVariable Long articleId)
    {
        String username = getCurrentUsernameOrNull();
        ReactionResponseDTO response = reactionService.getReactions(articleId, username);
        return ResponseEntity.ok(ResponseWrapper.<ReactionResponseDTO>builder()
                .status(HttpStatus.OK)
                .code(200)
                .data(response)
                .build());
    }

    private String getCurrentUsername()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    private String getCurrentUsernameOrNull()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated())
        {
            return authentication.getName();
        }
        return null;
    }

    @GetMapping("/liked-articles")
    public ResponseEntity<ResponseWrapper<List<ArticlesResponseDTO>>> getLikedArticles()
    {
        String username = getCurrentUsername();
        List<ArticlesResponseDTO> articles = reactionService.getLikedArticles(username);
        return ResponseEntity.ok(ResponseWrapper.<List<ArticlesResponseDTO>>builder()
                .status(HttpStatus.OK)
                .code(200)
                .data(articles)
                .build());
    }
}

