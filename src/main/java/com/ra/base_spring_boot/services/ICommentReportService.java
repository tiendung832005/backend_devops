package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.req.ReportCommentRequestDTO;
import com.ra.base_spring_boot.dto.resp.ReportCommentResponseDTO;

public interface ICommentReportService {
    ReportCommentResponseDTO reportComment(ReportCommentRequestDTO dto, String username);
}

