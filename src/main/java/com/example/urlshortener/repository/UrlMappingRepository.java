package com.example.urlshortener.repository;

import com.example.urlshortener.entity.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

    Optional<UrlMapping> findByShortCode(String shortCode);

    @Query("SELECT u FROM UrlMapping u WHERE u.originalUrlHash = :hash AND u.expiresAt > :now")
    Optional<UrlMapping> findActiveByOriginalUrlHash(String hash, LocalDateTime now);

    boolean existsByShortCode(String shortCode);

}
