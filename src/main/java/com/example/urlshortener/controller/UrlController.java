package com.example.urlshortener.controller;

import com.example.urlshortener.dto.ResolveResponse;
import com.example.urlshortener.dto.ShortenRequest;
import com.example.urlshortener.dto.ShortenResponse;
import com.example.urlshortener.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlShortenerService service;


    @PostMapping("/api/shorten")
    public ResponseEntity<ShortenResponse> shorten(@Valid @RequestBody ShortenRequest request) {
        ShortenResponse response = service.shorten(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/r/{shortCode}")
    public ResponseEntity<?> resolve(
            @PathVariable String shortCode,
            @RequestHeader(value = "Accept", defaultValue = "text/html") String acceptHeader,
            HttpServletResponse response) throws IOException {

        if (shortCode == null || shortCode.isBlank() || shortCode.length() > 10) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid short code"));
        }

        ResolveResponse resolveResponse = service.resolve(shortCode);

        if (acceptHeader.contains("text/html")) {
            response.sendRedirect(resolveResponse.getOriginalUrl());
            return null;
        }

        return ResponseEntity.ok(resolveResponse);
    }
}