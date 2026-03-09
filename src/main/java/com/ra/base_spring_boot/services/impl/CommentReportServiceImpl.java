package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.req.ReportCommentRequestDTO;
import com.ra.base_spring_boot.dto.resp.ReportCommentResponseDTO;
import com.ra.base_spring_boot.exception.HttpConflict;
import com.ra.base_spring_boot.exception.HttpForbiden;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.entity.CommentReport;
import com.ra.base_spring_boot.model.entity.Comments;
import com.ra.base_spring_boot.model.entity.User;
import com.ra.base_spring_boot.repository.ICommentReportRepository;
import com.ra.base_spring_boot.repository.ICommentsRepository;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.services.ICommentReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentReportServiceImpl implements ICommentReportService {
    
    private final ICommentReportRepository commentReportRepository;
    private final ICommentsRepository commentsRepository;
    private final IUserRepository userRepository;
    
    private static final int REPORT_THRESHOLD = 3; // Số lượng báo cáo cần thiết để ẩn comment

    @Override
    @Transactional
    public ReportCommentResponseDTO reportComment(ReportCommentRequestDTO dto, String username) {
        // Check if comment exists
        Comments comment = commentsRepository.findById(dto.getCommentId())
                .orElseThrow(() -> new HttpNotFound("Comment not found with id: " + dto.getCommentId()));
        
        // Get reporter
        User reporter = userRepository.findByUsername(username)
                .orElseThrow(() -> new HttpNotFound("User not found with username: " + username));
        
        // Check if user is already banned
        if (reporter.getStatus() != null) {
            throw new HttpForbiden("Your account has been banned");
        }
        
        // Check if user is trying to report their own comment
        if (comment.getUser().getId().equals(reporter.getId())) {
            throw new HttpConflict("Cannot report your own comment");
        }
        
        // Check if already reported by this user
        if (commentReportRepository.existsByComment_IdAndReporter_Id(dto.getCommentId(), reporter.getId())) {
            throw new HttpConflict("You have already reported this comment");
        }
        
        // Create report
        CommentReport report = new CommentReport();
        report.setComment(comment);
        report.setReporter(reporter);
        report.setReason(dto.getReason());
        CommentReport savedReport = commentReportRepository.save(report);
        
        // Check if comment has reached the threshold (3 reports from 3 different users)
        Long distinctReportersCount = commentReportRepository.countDistinctReportersByCommentId(dto.getCommentId());
        
        if (distinctReportersCount >= REPORT_THRESHOLD) {
            // Hide comment and wait for admin approval
            comment.setIsHidden(true);
            commentsRepository.save(comment);
        }
        
        return new ReportCommentResponseDTO(savedReport);
    }
}

