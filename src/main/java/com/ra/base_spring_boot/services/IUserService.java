package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.req.UserUpdateRequest;
import com.ra.base_spring_boot.model.constants.StatusUser;
import com.ra.base_spring_boot.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import com.ra.base_spring_boot.dto.resp.UserResponseDTO;

import java.util.List;

public interface IUserService {
    List<User> getAllUsers();

    List<User> importUsers(MultipartFile file) throws Exception;

    ByteArrayInputStream exportUsersExcel();

    UserResponseDTO getCurrentUserProfile(String username);

    UserResponseDTO getUserById(Long id);

    UserResponseDTO updateCurrentUserProfile(String username, String fullName, String email, String avatarUrl,
            String bio);

    Page<UserResponseDTO> getUsers(String keyword, StatusUser status, String role, int page, int size, String sortBy, String direction);

    void blockUser(Long id);
    void deleteUser(Long id);

    void updateUser(Long id, UserUpdateRequest request);
}
