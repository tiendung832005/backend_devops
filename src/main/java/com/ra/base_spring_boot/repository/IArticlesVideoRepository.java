package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.entity.ArticlesVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IArticlesVideoRepository extends JpaRepository<ArticlesVideo, Long> {
}

