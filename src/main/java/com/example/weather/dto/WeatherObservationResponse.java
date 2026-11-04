package com.example.weather.dto;

import java.time.LocalDateTime;

public record WeatherObservationResponse(
        Long id,
        String city,
        double temperature,
        int humidity,
        double windSpeed,
        String condition,
        LocalDateTime observedAt,
        LocalDateTime createdAt) {
}

