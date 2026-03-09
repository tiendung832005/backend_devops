package com.ra.base_spring_boot.dto.req;

import com.ra.base_spring_boot.model.constants.ContentType;
import com.ra.base_spring_boot.model.constants.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StaticContentRequest {
    @NotBlank
    private String code;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @NotNull
    private ContentType contentType;

    @NotNull
    private Status status;
}
