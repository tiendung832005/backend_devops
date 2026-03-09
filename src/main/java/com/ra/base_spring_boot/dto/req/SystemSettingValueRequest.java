package com.ra.base_spring_boot.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SystemSettingValueRequest {

    @NotBlank(message = "Value is required")
    private String value;
}

