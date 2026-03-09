package com.ra.base_spring_boot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ResponseWrapper<T> {
    private int code;
    private HttpStatus status;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    private Object meta;
}

