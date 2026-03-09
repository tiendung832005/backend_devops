package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.resp.UserResponseDTO;
import com.ra.base_spring_boot.exception.HttpConflict;
import com.ra.base_spring_boot.exception.HttpForbiden;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.model.entity.FollowAuthors;
import com.ra.base_spring_boot.model.entity.Roles;
import com.ra.base_spring_boot.model.entity.User;
import com.ra.base_spring_boot.repository.IFollowAuthorRepository;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.services.IFollowAuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowAuthorServiceImpl implements IFollowAuthorService {
    
    private final IFollowAuthorRepository followAuthorRepository;
    private final IUserRepository userRepository;

    @Override
    @Transactional
    public UserResponseDTO followAuthor(Long authorId, String username) {
        // Check if author exists
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new HttpNotFound("Author not found with id: " + authorId));
        
        // Check if author has ROLE_WRITER
        Set<Roles> authorRoles = author.getRoles();
        boolean isWriter = authorRoles.stream()
                .anyMatch(role -> role.getRoleName() == RoleName.ROLE_WRITER);
        
        if (!isWriter) {
            throw new HttpForbiden("User with id " + authorId + " is not a writer (ROLE_WRITER)");
        }
        
        // Get current user (follower)
        User follower = userRepository.findByUsername(username)
                .orElseThrow(() -> new HttpNotFound("User not found with username: " + username));
        
        // Check if trying to follow themselves
        if (author.getId().equals(follower.getId())) {
            throw new HttpConflict("Cannot follow yourself");
        }
        
        // Check if already following
        if (followAuthorRepository.existsByAuthor_IdAndFollower_Id(authorId, follower.getId())) {
            throw new HttpConflict("Already following this author");
        }
        
        // Create follow relationship
        FollowAuthors follow = new FollowAuthors();
        follow.setAuthor(author);
        follow.setFollower(follower);
        followAuthorRepository.save(follow);
        
        return new UserResponseDTO(author);
    }

    @Override
    @Transactional
    public void unfollowAuthor(Long authorId, String username) {
        User follower = userRepository.findByUsername(username)
                .orElseThrow(() -> new HttpNotFound("User not found with username: " + username));
        
        FollowAuthors follow = followAuthorRepository.findByAuthor_IdAndFollower_Id(authorId, follower.getId())
                .orElseThrow(() -> new HttpNotFound("You are not following this author"));
        
        followAuthorRepository.delete(follow);
    }

    @Override
    public List<UserResponseDTO> getFollowedAuthors(String username) {
        User follower = userRepository.findByUsername(username)
                .orElseThrow(() -> new HttpNotFound("User not found with username: " + username));
        
        List<User> followedAuthors = followAuthorRepository.findFollowedAuthorsByFollowerId(follower.getId());
        
        return followedAuthors.stream()
                .map(UserResponseDTO::new)
                .collect(Collectors.toList());
    }
}

