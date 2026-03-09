package com.ra.base_spring_boot.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articleId")
    private Articles article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    @Column(nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Comments parent;

    @OneToMany(mappedBy = "parent")
    private List<Comments> replies = new ArrayList<>();

    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = false;

    @Column(name = "hidden_reason", nullable = true, length = 500)
    private String hiddenReason;

    @Column(name = "hidden_by", nullable = true, length = 50)
    private String hiddenBy; // SYSTEM_SPAM, SYSTEM_BANNED_KEYWORD, ADMIN

    @Column(name = "hidden_at", nullable = true)
    private LocalDateTime hiddenAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

}