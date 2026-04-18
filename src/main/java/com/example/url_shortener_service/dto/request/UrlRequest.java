package com.example.url_shortener_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UrlRequest {

    @NotBlank(message = "URL is required")
    @Pattern(regexp = "^(https?://).+", message = "URL must start with http:// or https://")
    private String originalUrl;

    private Integer expiresInDays;
}