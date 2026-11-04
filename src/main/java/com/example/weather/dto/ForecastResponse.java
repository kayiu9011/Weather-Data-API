package com.example.weather.dto;

import java.util.List;

public record ForecastResponse(String city, List<HourlyForecast> hourly) {
}

