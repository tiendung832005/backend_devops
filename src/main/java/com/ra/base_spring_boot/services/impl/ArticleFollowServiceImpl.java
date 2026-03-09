package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.resp.ArticlesResponseDTO;
import com.ra.base_spring_boot.exception.HttpConflict;
import com.ra.base_spring_boot.exception.HttpForbiden;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.constants.ArticlesStatus;
import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.model.entity.ArticleFollow;
import com.ra.base_spring_boot.model.entity.Articles;
import com.ra.base_spring_boot.model.entity.Roles;
import com.ra.base_spring_boot.model.entity.User;
import com.ra.base_spring_boot.repository.IArticleFollowRepository;
import com.ra.base_spring_boot.repository.IArticlesRepository;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.services.IArticleFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ArticleFollowServiceImpl implements IArticleFollowService {
    private final IArticlesRepository articlesRepository;
    private final IUserRepository userRepository;
    private final IArticleFollowRepository articleFollowRepository;

    @Override
    @Transactional
    public ArticlesResponseDTO followArticle(Long articleId, String username) {
        // Check if article exists and is APPROVED
        Articles article = articlesRepository.findById(articleId)
                .orElseThrow(() -> new HttpNotFound("Article not found with id: " + articleId));
        
        if (article.getStatus() != ArticlesStatus.APPROVED) {
            throw new HttpNotFound("Article not found with id: " + articleId);
        }
        
        // Get user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new HttpNotFound("User not found with username: " + username));
        
        // Check user role (only ROLE_READER and ROLE_WRITER)
        Set<Roles> userRoles = user.getRoles();
        boolean hasValidRole = userRoles.stream()
                .anyMatch(role -> role.getRoleName() == RoleName.ROLE_READER || 
                                role.getRoleName() == RoleName.ROLE_WRITER);
        
        if (!hasValidRole) {
            throw new HttpForbiden("Only READER and WRITER roles can follow articles");
        }
        
        // Check if already followed
        if (articleFollowRepository.existsByArticle_IdAndUser_Id(articleId, user.getId())) {
            throw new HttpConflict("Article is already being followed");
        }
        
        // Create bookmark
        ArticleFollow bookmark = new ArticleFollow();
        bookmark.setArticle(article);
        bookmark.setUser(user);
        articleFollowRepository.save(bookmark);
        
        return new ArticlesResponseDTO(article);
    }

    @Override
    @Transactional
    public void unfollowArticle(Long articleId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new HttpNotFound("User not found with username: " + username));

        ArticleFollow bookmark = articleFollowRepository.findByArticle_IdAndUser_Id(articleId, user.getId())
                .orElseThrow(() -> new HttpNotFound("Article is not being followed"));

        articleFollowRepository.delete(bookmark);
    }

    @Override
    public Page<ArticlesResponseDTO> getFollowedArticles(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new HttpNotFound("User not found with username: " + username));
        
        Page<Articles> articles = articleFollowRepository.findArticlesByUserId(user.getId(), ArticlesStatus.APPROVED, pageable);
        return articles.map(ArticlesResponseDTO::new);
    }
}

