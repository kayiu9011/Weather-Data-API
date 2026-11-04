package com.example.weather.dto;

import java.time.LocalDateTime;

public record HourlyForecast(
        LocalDateTime time,
        double temperature,
        int rainProbability,
        String condition) {
}

