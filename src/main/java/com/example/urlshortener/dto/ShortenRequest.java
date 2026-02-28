package com.example.urlshortener.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

@Data
public class ShortenRequest {

    @NotBlank(message = "originalUrl must not be blank")
    @URL(message = "originalUrl must be a valid URL (include http:// or https://)")
    private String originalUrl;

    @NotNull(message = "validity must not be null")
    @Future(message = "validity must be a future date-time")
    private LocalDateTime validity;
}
