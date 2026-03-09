package com.ra.base_spring_boot.dto.resp;

import com.ra.base_spring_boot.model.constants.ReportReason;
import com.ra.base_spring_boot.model.entity.CommentReport;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportCommentResponseDTO {
    private Long id;
    private Long commentId;
    private Long reporterId;
    private String reporterUsername;
    private ReportReason reason;
    private LocalDateTime createdAt;

    public ReportCommentResponseDTO(CommentReport report) {
        this.id = report.getId();
        this.commentId = report.getComment().getId();
        this.reporterId = report.getReporter().getId();
        this.reporterUsername = report.getReporter().getUsername();
        this.reason = report.getReason();
        this.createdAt = report.getCreatedAt();
    }
}

