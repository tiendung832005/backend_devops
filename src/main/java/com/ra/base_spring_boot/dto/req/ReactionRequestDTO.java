package com.ra.base_spring_boot.dto.req;

import com.ra.base_spring_boot.model.constants.ReactionType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReactionRequestDTO
{
    @NotNull(message = "articleId is required")
    private Long articleId;

    @NotNull(message = "reactionType is required")
    private ReactionType reactionType;
}

