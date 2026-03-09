package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.resp.ArticlesResponseDTO;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.constants.ArticlesStatus;
import com.ra.base_spring_boot.model.entity.Articles;
import com.ra.base_spring_boot.repository.IArticlesRepository;
import com.ra.base_spring_boot.services.IPublicArticlesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;

@Service
@RequiredArgsConstructor
public class PublicArticlesServiceImpl implements IPublicArticlesService {
    private final IArticlesRepository articlesRepository;

    @Override
    public Page<ArticlesResponseDTO> findAllApprovedArticles(Pageable pageable) {
        Specification<Articles> spec = (root, query, cb) -> {
            Predicate predicate = cb.equal(root.get("status"), ArticlesStatus.APPROVED);
            return predicate;
        };
        
        return articlesRepository.findAll(spec, pageable)
                .map(ArticlesResponseDTO::new);
    }

    @Override
    public Page<ArticlesResponseDTO> findApprovedArticlesByCategory(Long categoryId, Pageable pageable) {
        Specification<Articles> spec = (root, query, cb) -> {
            Predicate statusPredicate = cb.equal(root.get("status"), ArticlesStatus.APPROVED);
            Predicate categoryPredicate = cb.equal(root.get("category").get("id"), categoryId);
            return cb.and(statusPredicate, categoryPredicate);
        };
        
        return articlesRepository.findAll(spec, pageable)
                .map(ArticlesResponseDTO::new);
    }

    @Override
    public ArticlesResponseDTO findApprovedArticleById(Long id) {
        Specification<Articles> spec = (root, query, cb) -> {
            Predicate statusPredicate = cb.equal(root.get("status"), ArticlesStatus.APPROVED);
            Predicate idPredicate = cb.equal(root.get("id"), id);
            return cb.and(statusPredicate, idPredicate);
        };
        
        Articles article = articlesRepository.findOne(spec)
                .orElseThrow(() -> new HttpNotFound("Approved article not found with id: " + id));
        
        return new ArticlesResponseDTO(article);
    }
}

