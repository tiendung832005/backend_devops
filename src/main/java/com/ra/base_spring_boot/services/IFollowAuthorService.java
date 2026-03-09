package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.resp.UserResponseDTO;

import java.util.List;

public interface IFollowAuthorService {
    UserResponseDTO followAuthor(Long authorId, String username);
    void unfollowAuthor(Long authorId, String username);
    List<UserResponseDTO> getFollowedAuthors(String username);
}

