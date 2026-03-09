package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.resp.ArticlesResponseDTO;
import com.ra.base_spring_boot.exception.HttpConflict;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.entity.Articles;
import com.ra.base_spring_boot.model.entity.BookmarkArticle;
import com.ra.base_spring_boot.model.entity.User;
import com.ra.base_spring_boot.repository.IArticlesRepository;
import com.ra.base_spring_boot.repository.IBookmarkArticleRepository;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.services.IBookmarkArticleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookmarkArticleServiceImpl implements IBookmarkArticleService {

    private final IArticlesRepository articlesRepository;
    private final IUserRepository userRepository;
    private final IBookmarkArticleRepository bookmarkRepo;

    @Override
    @Transactional
    public ArticlesResponseDTO bookmarkArticle(Long articleId, String username) {

        Articles article = articlesRepository.findById(articleId)
                .orElseThrow(() -> new HttpNotFound("Article not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new HttpNotFound("User not found"));

        if (bookmarkRepo.existsByArticle_IdAndUser_Id(articleId, user.getId())) {
            throw new HttpConflict("This article is already bookmarked");
        }

        BookmarkArticle bookmark = new BookmarkArticle();
        bookmark.setArticle(article);
        bookmark.setUser(user);

        bookmarkRepo.save(bookmark);

        return new ArticlesResponseDTO(article);
    }

    @Override
    @Transactional
    public void removeBookmark(Long articleId, String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new HttpNotFound("User not found"));

        BookmarkArticle bookmark = bookmarkRepo
                .findByArticle_IdAndUser_Id(articleId, user.getId())
                .orElseThrow(() -> new HttpNotFound("Bookmark not found"));

        bookmarkRepo.delete(bookmark);
    }

    @Override
    public Page<ArticlesResponseDTO> getUserBookmarks(String username, Pageable pageable) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new HttpNotFound("User not found"));

        Page<Articles> articles =
                bookmarkRepo.findBookmarkedArticles(user.getId(), pageable);

        return articles.map(ArticlesResponseDTO::new);
    }
}
