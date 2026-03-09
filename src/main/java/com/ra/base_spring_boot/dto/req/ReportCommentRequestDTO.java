package com.ra.base_spring_boot.dto.req;

import com.ra.base_spring_boot.model.constants.ReportReason;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportCommentRequestDTO {
    
    @NotNull(message = "Comment ID is required")
    private Long commentId;
    
    @NotNull(message = "Report reason is required")
    private ReportReason reason;
}

