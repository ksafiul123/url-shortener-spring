package com.example.urlshortener.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "url_mappings",
        indexes = {
                @Index(name = "idx_short_code", columnList = "shortCode", unique = true),
                @Index(name = "idx_original_url_hash", columnList = "originalUrlHash")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String shortCode;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String originalUrl;

    @Column(nullable = false, length = 64)
    private String originalUrlHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}