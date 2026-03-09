package com.ra.base_spring_boot.dto.resp;

import com.ra.base_spring_boot.model.constants.ContentType;
import com.ra.base_spring_boot.model.constants.Status;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StaticContentResponse {
    private Long id;
    private String code;
    private String title;
    private String content;
    private ContentType contentType;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
