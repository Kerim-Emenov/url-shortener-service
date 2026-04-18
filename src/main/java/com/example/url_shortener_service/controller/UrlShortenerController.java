package com.example.url_shortener_service.controller;

import com.example.url_shortener_service.dto.request.UrlRequest;
import com.example.url_shortener_service.dto.response.UrlResponse;
import com.example.url_shortener_service.service.UrlShortenerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;

    @PostMapping("/api/shorten")
    public ResponseEntity<UrlResponse> shorten(@Valid @RequestBody UrlRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(urlShortenerService.shorten(request));
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        String originalUrl = urlShortenerService.resolve(shortCode);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, originalUrl);
        return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
    }

    @GetMapping("/api/urls")
    public ResponseEntity<List<UrlResponse>> getAll() {
        return ResponseEntity.ok(urlShortenerService.getAll());
    }

    @GetMapping("/api/urls/{shortCode}")
    public ResponseEntity<UrlResponse> getByShortCode(@PathVariable String shortCode) {
        return ResponseEntity.ok(urlShortenerService.getByShortCode(shortCode));
    }

    @DeleteMapping("/api/urls/{shortCode}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable String shortCode) {
        urlShortenerService.deactivate(shortCode);
        return ResponseEntity.noContent().build();
    }
}