package com.ra.base_spring_boot.dto.req;

import lombok.Data;

@Data
public class AdminSearchDTO {
    private String keyword;
    private Boolean status;
    private Long roleId;
}

