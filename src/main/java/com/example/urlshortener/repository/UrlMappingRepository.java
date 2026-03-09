package com.example.urlshortener.repository;

import com.example.urlshortener.entity.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

    Optional<UrlMapping> findByShortCode(String shortCode);

    @Query("SELECT u FROM UrlMapping u WHERE u.originalUrlHash = :hash")
    Optional<UrlMapping> findByOriginalUrlHash(String hash);

    @Modifying
    @Query("UPDATE UrlMapping u SET u.expiresAt = :expiresAt WHERE u.originalUrlHash = :hash")
    void updateExpiryByHash(String hash, LocalDateTime expiresAt);

    boolean existsByShortCode(String shortCode);

}
