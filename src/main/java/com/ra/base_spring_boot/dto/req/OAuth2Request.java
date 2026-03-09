package com.ra.base_spring_boot.dto.req;

import lombok.Data;

@Data
public class OAuth2Request {
    private String code;
    private String redirectUri;
}
