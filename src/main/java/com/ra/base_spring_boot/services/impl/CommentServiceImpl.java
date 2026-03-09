package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.req.CommentAdvancedFilterDTO;
import com.ra.base_spring_boot.dto.req.CommentRequestDTO;
import com.ra.base_spring_boot.dto.req.CommentSearchDTO;
import com.ra.base_spring_boot.dto.resp.ArticlesResponseDTO;
import com.ra.base_spring_boot.dto.resp.CommentResponseDTO;
import com.ra.base_spring_boot.exception.HttpForbiden;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.constants.StatusUser;
import com.ra.base_spring_boot.model.entity.Articles;
import com.ra.base_spring_boot.model.entity.Comments;
import com.ra.base_spring_boot.model.entity.User;
import com.ra.base_spring_boot.repository.IArticlesRepository;
import com.ra.base_spring_boot.repository.ICommentsRepository;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.services.IBannedKeywordService;
import com.ra.base_spring_boot.services.ICommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements ICommentService
{
    private final ICommentsRepository commentsRepository;
    private final IArticlesRepository articlesRepository;
    private final IUserRepository userRepository;
    private final IBannedKeywordService bannedKeywordService;

    @Override
    @Transactional
    public CommentResponseDTO create(CommentRequestDTO dto, String username)
    {
        Articles article = articlesRepository.findById(dto.getArticleId())
                .orElseThrow(() -> new HttpNotFound("Article not found with id: " + dto.getArticleId()));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new HttpNotFound("User not found with username: " + username));

        // Check if user is already banned
        if (user.getStatus() != null && user.getStatus() != StatusUser.ACTIVE) {
            throw new HttpForbiden("Your account has been banned");
        }

        Comments parent = null;
        if (dto.getParentId() != null)
        {
            parent = commentsRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new HttpNotFound("Parent comment not found with id: " + dto.getParentId()));
            if (!parent.getArticle().getId().equals(article.getId()))
            {
                throw new HttpNotFound("Parent comment does not belong to this article");
            }
        }

        Comments comment = new Comments();
        comment.setArticle(article);
        comment.setUser(user);
        comment.setContent(dto.getContent());
        comment.setParent(parent);

        // Check if comment contains banned keywords - if yes, hide the comment
        boolean containsBannedKeyword = bannedKeywordService.containsBannedKeyword(dto.getContent());
        boolean isSpam = isSpamComment(user, dto.getContent());

        if (containsBannedKeyword || isSpam) {
            comment.setIsHidden(true);
            comment.setHiddenAt(LocalDateTime.now());
            if (containsBannedKeyword) {
                comment.setHiddenBy("SYSTEM_BANNED_KEYWORD");
                comment.setHiddenReason("Contains banned keyword");
            } else if (isSpam) {
                comment.setHiddenBy("SYSTEM_SPAM");
                comment.setHiddenReason("Spam detected");
            }
        }

        Comments saved = commentsRepository.save(comment);
        return new CommentResponseDTO(saved);
    }

    @Override
    public List<CommentResponseDTO> getByArticle(Long articleId)
    {
        // Get top-level comments (only non-hidden ones)
        List<Comments> roots = commentsRepository.findByArticle_IdAndParentIsNullAndIsHiddenFalseOrderByCreatedAtAsc(articleId);
        return roots.stream()
                .map(this::buildTree)
                .collect(Collectors.toList());
    }

    private CommentResponseDTO buildTree(Comments comment)
    {
        CommentResponseDTO dto = new CommentResponseDTO(comment);
        // Get replies (only non-hidden ones)
        List<Comments> replies = commentsRepository.findByParent_IdAndIsHiddenFalseOrderByCreatedAtAsc(comment.getId());
        if (!replies.isEmpty())
        {
            dto.setReplies(
                    replies.stream()
                            .map(this::buildTree)
                            .collect(Collectors.toList())
            );
        }
        return dto;
    }

    @Override
    public List<ArticlesResponseDTO> getCommentedArticles(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new HttpNotFound("User not found with username: " + username));

        List<Comments> comments = commentsRepository.findByUser_Id(user.getId());

        // Get unique articles from comments
        Set<Articles> uniqueArticles = comments.stream()
                .map(Comments::getArticle)
                .collect(Collectors.toSet());

        return uniqueArticles.stream()
                .map(ArticlesResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CommentResponseDTO> search(CommentSearchDTO dto, Pageable pageable) {
        String keyword = dto != null ? dto.getKeyword() : null;
        Long articleId = dto != null ? dto.getArticleId() : null;
        Long userId = dto != null ? dto.getUserId() : null;

        return commentsRepository.search(keyword, articleId, userId, pageable)
                .map(CommentResponseDTO::new);
    }

    @Override
    public Page<CommentResponseDTO> advancedFilter(CommentAdvancedFilterDTO dto, String username, Pageable pageable) {
        return commentsRepository.advancedFilter(
                dto.getKeyword(),
                dto.getArticleId(),
                dto.getUserId(),
                dto.getStartDate(),
                dto.getEndDate(),
                pageable
        ).map(CommentResponseDTO::new);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!commentsRepository.existsById(id)) {
            throw new HttpNotFound("Comment not found with id: " + id);
        }
        commentsRepository.deleteById(id);
    }

    @Override
    @Transactional
    public CommentResponseDTO toggleHidden(Long id, String reason, String username) {
        Comments comment = commentsRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Comment not found with id: " + id));
        
        boolean newHiddenState = !comment.getIsHidden();
        comment.setIsHidden(newHiddenState);
        
        if (newHiddenState) {
            // Admin đang ẩn comment
            comment.setHiddenBy("ADMIN");
            comment.setHiddenReason(reason != null && !reason.trim().isEmpty() 
                    ? reason 
                    : "Hidden by admin");
            comment.setHiddenAt(LocalDateTime.now());
        } else {
            // Admin đang hiện lại comment (xóa thông tin ẩn trước đó)
            comment.setHiddenBy(null);
            comment.setHiddenReason(null);
            comment.setHiddenAt(null);
        }
        
        Comments saved = commentsRepository.save(comment);
        return new CommentResponseDTO(saved);
    }

    private boolean isSpamComment(User user, String content) {

        String trimmed = content.trim();

        // 1. Hashtag-only → không spam
        if (trimmed.matches("^(#\\w+\\s*)+$")) {
            return false;
        }

        // 2. Comment ngắn phổ biến → không spam
        if (trimmed.length() <= 10) {
            return false;
        }

        // 3. Đếm link
        Pattern pattern = Pattern.compile("(https?://\\S+)|(www\\.\\S+)");
        Matcher matcher = pattern.matcher(content);

        int linkCount = 0;
        while (matcher.find()) linkCount++;

        // 4. Nhiều link → spam
        if (linkCount >= 2) {
            return true;
        }

        // 5. Trùng nội dung + có link → spam
        if (linkCount >= 1 && commentsRepository.existsByContent(content)) {
            return true;
        }

        // 6. Trùng nội dung (không link) nhưng spam theo tần suất
        LocalDateTime sinceDuplicate = LocalDateTime.now().minusMinutes(1);
        long sameContentCount =
                commentsRepository.countSameContentByUser(
                        user.getId(), content, sinceDuplicate);

        if (linkCount == 0 && sameContentCount >= 3) {
            return true;
        }

        // 7. Spam theo tần suất chung (3 comment / 30s)
        LocalDateTime since = LocalDateTime.now().minusSeconds(30);
        long recentCount =
                commentsRepository.countRecentByUser(user.getId(), since);

        return recentCount >= 3;
    }


}


