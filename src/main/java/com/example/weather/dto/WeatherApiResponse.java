package com.example.weather.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WeatherApiResponse(CurrentWeather current, HourlyWeather hourly) {

    public record CurrentWeather(
            String time,
            @JsonProperty("temperature_2m") double temperature,
            @JsonProperty("relative_humidity_2m") int humidity,
            @JsonProperty("weather_code") int weatherCode,
            @JsonProperty("wind_speed_10m") double windSpeed) {
    }

    public record HourlyWeather(
            List<String> time,
            @JsonProperty("temperature_2m") List<Double> temperatures,
            @JsonProperty("precipitation_probability") List<Integer> rainProbabilities,
            @JsonProperty("weather_code") List<Integer> weatherCodes) {
    }
}

