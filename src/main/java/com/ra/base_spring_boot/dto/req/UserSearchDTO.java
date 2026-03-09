package com.ra.base_spring_boot.dto.req;

import lombok.Data;

@Data
public class UserSearchDTO {
    private String keyword;
    private String status;
    private Long roleId;
}
