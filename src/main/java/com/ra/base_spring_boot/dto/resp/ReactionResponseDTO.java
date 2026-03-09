package com.ra.base_spring_boot.dto.resp;

import com.ra.base_spring_boot.model.constants.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ReactionResponseDTO
{
    private Long articleId;
    private long likes;
    private long dislikes;
    private ReactionType currentUserReaction; // null nếu chưa react
}

