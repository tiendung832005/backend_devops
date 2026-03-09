package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.entity.StaticContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaticContentRepository extends JpaRepository<StaticContent, Long> {

    Optional<StaticContent> findByCode(String code);

    boolean existsByCode(String code);
}
