package com.example.url_shortener_service.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UrlResponse {
    private Long id;
    private String shortCode;
    private String originalUrl;
    private String shortUrl;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Long clickCount;
    private Boolean active;
}