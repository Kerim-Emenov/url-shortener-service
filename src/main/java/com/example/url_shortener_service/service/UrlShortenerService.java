package com.example.url_shortener_service.service;

import com.example.url_shortener_service.dto.request.UrlRequest;
import com.example.url_shortener_service.dto.response.UrlResponse;
import com.example.url_shortener_service.entity.UrlMapping;
import com.example.url_shortener_service.exception.ResourceNotFoundException;
import com.example.url_shortener_service.repository.UrlMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UrlShortenerService {

    private final UrlMappingRepository urlMappingRepository;

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SHORT_CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    public UrlResponse shorten(UrlRequest request) {
        String shortCode = generateUniqueShortCode();

        UrlMapping mapping = new UrlMapping();
        mapping.setShortCode(shortCode);
        mapping.setOriginalUrl(request.getOriginalUrl());

        if (request.getExpiresInDays() != null && request.getExpiresInDays() > 0) {
            mapping.setExpiresAt(LocalDateTime.now().plusDays(request.getExpiresInDays()));
        }

        return toResponse(urlMappingRepository.save(mapping));
    }

    @Cacheable(value = "urls", key = "#shortCode")
    public String resolve(String shortCode) {
        UrlMapping mapping = urlMappingRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("Short URL not found: " + shortCode));

        if (!mapping.getActive()) {
            throw new IllegalArgumentException("This short URL has been deactivated");
        }

        if (mapping.getExpiresAt() != null && mapping.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("This short URL has expired");
        }

        mapping.setClickCount(mapping.getClickCount() + 1);
        urlMappingRepository.save(mapping);

        return mapping.getOriginalUrl();
    }

    public List<UrlResponse> getAll() {
        return urlMappingRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public UrlResponse getByShortCode(String shortCode) {
        return toResponse(urlMappingRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("Short URL not found: " + shortCode)));
    }

    @CacheEvict(value = "urls", key = "#shortCode")
    public void deactivate(String shortCode) {
        UrlMapping mapping = urlMappingRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("Short URL not found: " + shortCode));
        mapping.setActive(false);
        urlMappingRepository.save(mapping);
    }

    private String generateUniqueShortCode() {
        String shortCode;
        do {
            shortCode = generateShortCode();
        } while (urlMappingRepository.existsByShortCode(shortCode));
        return shortCode;
    }

    private String generateShortCode() {
        StringBuilder sb = new StringBuilder(SHORT_CODE_LENGTH);
        for (int i = 0; i < SHORT_CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    private UrlResponse toResponse(UrlMapping mapping) {
        UrlResponse response = new UrlResponse();
        response.setId(mapping.getId());
        response.setShortCode(mapping.getShortCode());
        response.setOriginalUrl(mapping.getOriginalUrl());
        response.setShortUrl("http://localhost:8080/" + mapping.getShortCode());
        response.setCreatedAt(mapping.getCreatedAt());
        response.setExpiresAt(mapping.getExpiresAt());
        response.setClickCount(mapping.getClickCount());
        response.setActive(mapping.getActive());
        return response;
    }
}