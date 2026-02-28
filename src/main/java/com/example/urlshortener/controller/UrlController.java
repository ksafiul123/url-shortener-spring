package com.example.urlshortener.controller;

import com.example.urlshortener.dto.ResolveResponse;
import com.example.urlshortener.dto.ShortenRequest;
import com.example.urlshortener.dto.ShortenResponse;
import com.example.urlshortener.service.UrlShortenerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ResolveResponse> resolve(@PathVariable String shortCode) {
        if (shortCode == null || shortCode.isBlank() || shortCode.length() > 10) {
            return ResponseEntity.badRequest().build();
        }
        ResolveResponse response = service.resolve(shortCode);
        return ResponseEntity.ok(response);
    }
}