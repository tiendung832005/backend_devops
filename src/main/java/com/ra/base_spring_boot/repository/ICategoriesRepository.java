package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.entity.Categories;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICategoriesRepository extends JpaRepository<Categories, Long> {

    @Query("SELECT c, COALESCE((SELECT SUM(a.highlightLevel) FROM Articles a WHERE a.category.id = c.id), 0L) " +
            "FROM Categories c " +
            "ORDER BY COALESCE((SELECT SUM(a.highlightLevel) FROM Articles a WHERE a.category.id = c.id), 0L) DESC")
    List<Object[]> findAllWithTotalHighlightLevel();

    @Query("SELECT new com.ra.base_spring_boot.dto.resp.CategoryViewDTO(c.name, COALESCE(SUM(a.viewCount), 0L)) " +
            "FROM Categories c LEFT JOIN c.articles a " +
            "GROUP BY c.id, c.name")
    List<com.ra.base_spring_boot.dto.resp.CategoryViewDTO> getCategoryViews();

    Page<Categories> findAllByNameContainingIgnoreCase(String name, Pageable pageable);
}
