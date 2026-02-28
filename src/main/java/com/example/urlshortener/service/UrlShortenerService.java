package com.example.urlshortener.service;

import com.example.urlshortener.dto.ResolveResponse;
import com.example.urlshortener.dto.ShortenRequest;
import com.example.urlshortener.dto.ShortenResponse;
import com.example.urlshortener.entity.UrlMapping;
import com.example.urlshortener.exception.UrlExpiredException;
import com.example.urlshortener.exception.UrlNotFoundException;
import com.example.urlshortener.repository.UrlMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlShortenerService {

    private final UrlMappingRepository repository;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.short-code-length:6}")
    private int shortCodeLength;

    private static final String ALPHABET =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_COLLISION_RETRIES = 10;

    @Transactional
    public ShortenResponse shorten(ShortenRequest request) {
        String originalUrl = request.getOriginalUrl().trim();
        LocalDateTime expiresAt = request.getValidity();


        String urlHash = sha256(originalUrl);


        Optional<UrlMapping> existingOpt = repository.findActiveByOriginalUrlHash(urlHash, LocalDateTime.now());
        if (existingOpt.isPresent()) {
            UrlMapping existing = existingOpt.get();
            log.info("Returning existing short code for duplicate URL: {}", existing.getShortCode());
            return buildShortenResponse(existing);
        }


        String shortCode = generateUniqueShortCode();

        UrlMapping mapping = UrlMapping.builder()
                .shortCode(shortCode)
                .originalUrl(originalUrl)
                .originalUrlHash(urlHash)
                .expiresAt(expiresAt)
                .build();

        repository.save(mapping);
        log.info("Created new short code '{}' for URL: {}", shortCode, originalUrl);
        return buildShortenResponse(mapping);
    }

    @Transactional(readOnly = true)
    public ResolveResponse resolve(String shortCode) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("Short URL not found: " + shortCode));

        if (LocalDateTime.now().isAfter(mapping.getExpiresAt())) {
            throw new UrlExpiredException("URL expired");
        }

        return ResolveResponse.builder()
                .originalUrl(mapping.getOriginalUrl())
                .expiresAt(mapping.getExpiresAt())
                .build();
    }



    private String generateUniqueShortCode() {
        for (int attempt = 0; attempt < MAX_COLLISION_RETRIES; attempt++) {
            String code = randomCode(shortCodeLength);
            if (!repository.existsByShortCode(code)) {
                return code;
            }
            log.warn("Short code collision on attempt {}: {}", attempt + 1, code);
        }
        // Fallback: try with longer code to further reduce collision chances
        for (int attempt = 0; attempt < MAX_COLLISION_RETRIES; attempt++) {
            String code = randomCode(shortCodeLength + 2);
            if (!repository.existsByShortCode(code)) {
                return code;
            }
        }
        throw new RuntimeException("Failed to generate unique short code after retries. Please try again.");
    }

    private String randomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private ShortenResponse buildShortenResponse(UrlMapping mapping) {
        return ShortenResponse.builder()
                .shortUrl(baseUrl + "/r/" + mapping.getShortCode())
                .originalUrl(mapping.getOriginalUrl())
                .expiresAt(mapping.getExpiresAt())
                .createdAt(mapping.getCreatedAt())
                .build();
    }
}