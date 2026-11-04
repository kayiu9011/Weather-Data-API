package com.example.weather.dto;

import java.time.LocalDateTime;

public record HottestHourResponse(String city, LocalDateTime time, double temperature) {
}

