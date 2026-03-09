package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.ResponseWrapper;
import com.ra.base_spring_boot.dto.req.UserProfileUpdateDTO;
import com.ra.base_spring_boot.dto.req.UserUpdateRequest;
import com.ra.base_spring_boot.dto.resp.UserResponseDTO;
import com.ra.base_spring_boot.model.constants.StatusUser;
import com.ra.base_spring_boot.services.IUserService;
import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.Map;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(ResponseWrapper.builder()
                .data(userService.getAllUsers())
                .code(202)
                .status(HttpStatus.OK)
                .build());
    }

    @GetMapping()
    public ResponseEntity<?> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) StatusUser status,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "username") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Page<UserResponseDTO> result = userService.getUsers(
                keyword, status, role, page, size, sortBy, direction
        );

        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .data(result.getContent())
                        .meta(Map.of(
                                "page", result.getNumber(),
                                "size", result.getSize(),
                                "totalElements", result.getTotalElements(),
                                "totalPages", result.getTotalPages()
                        ))
                        .code(200)
                        .status(HttpStatus.OK)
                        .build()
        );
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getDetailUsers(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseWrapper.builder()
                .data(userService.getUserById(id))
                .code(200)
                .status(HttpStatus.OK)
                .build());
    }


    @PostMapping("/import")
    public ResponseEntity<?> importUsers(@RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.builder()
                .status(HttpStatus.CREATED)
                .code(201)
                .data(userService.importUsers(file))
                .build());
    }

    @GetMapping("/export")
    public ResponseEntity<?> exportUsers() {
        ByteArrayInputStream inputStream = userService.exportUsersExcel();

        byte[] bytes;
        try {
            bytes = inputStream.readAllBytes();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Cannot generate file");
        }

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=users.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile() {
        String username = getCurrentUsername();
        UserResponseDTO profile = userService.getCurrentUserProfile(username);
        return ResponseEntity.ok(ResponseWrapper.builder()
                .data(profile)
                .code(200)
                .status(HttpStatus.OK)
                .build());
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateCurrentUserProfile(@RequestBody UserProfileUpdateDTO dto) {
        String username = getCurrentUsername();
        UserResponseDTO updatedProfile = userService.updateCurrentUserProfile(
                username,
                dto.getFullName(),
                dto.getEmail(),
                dto.getAvatarUrl(),
                dto.getBio()
        );
        return ResponseEntity.ok(ResponseWrapper.builder()
                .data(updatedProfile)
                .code(200)
                .status(HttpStatus.OK)
                .build());
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
    @PutMapping("/block/{id}")
    public ResponseEntity<?> blockUser(@PathVariable Long id) {
        userService.blockUser(id);

        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .code(200)
                        .status(HttpStatus.OK)
                        .message("User status updated successfully")
                        .build()
        );
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);

        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .code(200)
                        .status(HttpStatus.OK)
                        .message("User deleted successfully")
                        .build()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest request
    ) {
        userService.updateUser(id, request);
        return ResponseEntity.ok("Update user successfully");
    }

}
