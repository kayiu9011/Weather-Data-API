package com.example.weather.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.weather.dto.AverageTemperatureResponse;
import com.example.weather.dto.CurrentWeatherResponse;
import com.example.weather.dto.ForecastResponse;
import com.example.weather.dto.HottestHourResponse;
import com.example.weather.dto.UmbrellaResponse;
import com.example.weather.dto.WeatherObservationResponse;
import com.example.weather.service.WeatherService;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/current")
    public CurrentWeatherResponse getCurrentWeather(@RequestParam String city) {
        return weatherService.getCurrentWeather(city);
    }

    @GetMapping("/forecast")
    public ForecastResponse getForecast(@RequestParam String city) {
        return weatherService.getForecast(city);
    }

    @GetMapping("/average")
    public AverageTemperatureResponse getAverageTemperature(@RequestParam String city) {
        return weatherService.getAverageTemperature(city);
    }

    @GetMapping("/hottest")
    public HottestHourResponse getHottestHour(@RequestParam String city) {
        return weatherService.getHottestHour(city);
    }

    @GetMapping("/umbrella")
    public UmbrellaResponse getUmbrellaRecommendation(@RequestParam String city) {
        return weatherService.getUmbrellaRecommendation(city);
    }

    @GetMapping("/history")
    public List<WeatherObservationResponse> getHistory(
            @RequestParam String city,
            @RequestParam(defaultValue = "10") int limit) {
        return weatherService.getHistory(city, limit);
    }
}

