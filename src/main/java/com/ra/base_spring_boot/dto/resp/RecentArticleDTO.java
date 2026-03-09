package com.ra.base_spring_boot.dto.resp;

import com.ra.base_spring_boot.model.constants.ArticlesStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecentArticleDTO {

    // Tiêu đề bài viết
    private String name;

    // Email tác giả
    private String email;

    // Trạng thái bài viết
    private ArticlesStatus amount;
}
