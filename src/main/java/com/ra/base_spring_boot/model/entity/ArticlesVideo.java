package com.ra.base_spring_boot.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "articles_video")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticlesVideo {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "article_id")
    private Articles article;

    @Column(nullable = false)
    private String videoUrl;

    @Column(nullable = true)
    private String description;

    @Column(nullable = true)
    private Integer duration;
}
