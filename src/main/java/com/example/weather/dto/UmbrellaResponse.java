package com.example.weather.dto;

public record UmbrellaResponse(
        String city,
        boolean umbrellaRecommended,
        String message) {
}

