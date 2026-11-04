package com.example.weather.dto;

import java.time.LocalDateTime;

public record CurrentWeatherResponse(
        String city,
        double temperature,
        int humidity,
        double windSpeed,
        String condition,
        LocalDateTime observedAt) {
}

