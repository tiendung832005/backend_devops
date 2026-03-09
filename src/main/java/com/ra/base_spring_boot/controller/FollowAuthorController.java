package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.ResponseWrapper;
import com.ra.base_spring_boot.dto.resp.UserResponseDTO;
import com.ra.base_spring_boot.services.IFollowAuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user/authors")
@RequiredArgsConstructor
public class FollowAuthorController {
    
    private final IFollowAuthorService followAuthorService;

    /**
     * @apiNote Follow an author (must be ROLE_WRITER)
     */
    @PostMapping("/{authorId}/follow")
    public ResponseEntity<ResponseWrapper<UserResponseDTO>> followAuthor(@PathVariable Long authorId) {
        String username = getCurrentUsername();
        UserResponseDTO author = followAuthorService.followAuthor(authorId, username);
        return ResponseEntity.ok(ResponseWrapper.<UserResponseDTO>builder()
                .status(HttpStatus.OK)
                .code(200)
                .data(author)
                .build());
    }

    /**
     * @apiNote Unfollow an author
     */
    @DeleteMapping("/{authorId}/follow")
    public ResponseEntity<ResponseWrapper<String>> unfollowAuthor(@PathVariable Long authorId) {
        String username = getCurrentUsername();
        followAuthorService.unfollowAuthor(authorId, username);
        return ResponseEntity.ok(ResponseWrapper.<String>builder()
                .status(HttpStatus.OK)
                .code(200)
                .data("Author unfollowed successfully")
                .build());
    }

    /**
     * @apiNote Get all followed authors of current user
     */
    @GetMapping("/followed")
    public ResponseEntity<ResponseWrapper<List<UserResponseDTO>>> getFollowedAuthors() {
        String username = getCurrentUsername();
        List<UserResponseDTO> authors = followAuthorService.getFollowedAuthors(username);
        return ResponseEntity.ok(ResponseWrapper.<List<UserResponseDTO>>builder()
                .status(HttpStatus.OK)
                .code(200)
                .data(authors)
                .build());
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}

