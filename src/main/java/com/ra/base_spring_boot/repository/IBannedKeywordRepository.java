package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.entity.BannedKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IBannedKeywordRepository extends JpaRepository<BannedKeyword, Long> {
    Optional<BannedKeyword> findByKeywordIgnoreCase(String keyword);
    List<BannedKeyword> findAllByOrderByCreatedAtDesc();
}

