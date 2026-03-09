package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.constants.ArticlesStatus;
import com.ra.base_spring_boot.model.entity.Articles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface IArticlesRepository extends JpaRepository<Articles, Long>, JpaSpecificationExecutor<Articles> {
    int countArticlesByStatus(ArticlesStatus status);
}

