package com.ra.base_spring_boot.model.entity;

import com.ra.base_spring_boot.model.constants.ArticlesStatus;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "articles_history")
public class ArticlesHistory{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articlesId")
    private Articles articles;

    private ArticlesStatus old_status;

    private ArticlesStatus new_status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changeBy")
    private User changeBy;
    @Temporal(TemporalType.TIMESTAMP)
    private Date changeAt;
}
