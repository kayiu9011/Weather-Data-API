package com.example.weather.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.example.weather.dto.CurrentWeatherResponse;
import com.example.weather.service.WeatherService;

@WebMvcTest(WeatherController.class)
class WeatherControllerTests {

    private final MockMvc mockMvc;

    @MockitoBean
    private WeatherService weatherService;

    @Autowired
    WeatherControllerTests(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void currentEndpointReturnsWeatherForRequestedCity() throws Exception {
        CurrentWeatherResponse response = new CurrentWeatherResponse(
                "Singapore", 30.2, 74, 8.4, "Partly Cloudy",
                LocalDateTime.of(2026, 11, 4, 12, 0));

        when(weatherService.getCurrentWeather("Singapore")).thenReturn(response);

        mockMvc.perform(get("/api/weather/current").param("city", "Singapore"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Singapore"))
                .andExpect(jsonPath("$.temperature").value(30.2))
                .andExpect(jsonPath("$.condition").value("Partly Cloudy"));
    }

    @Test
    void currentEndpointReturnsNotFoundForUnknownCity() throws Exception {
        when(weatherService.getCurrentWeather("NotARealCity"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "City not found"));

        mockMvc.perform(get("/api/weather/current").param("city", "NotARealCity"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("City not found"));
    }

    @Test
    void currentEndpointRejectsMissingCityParameter() throws Exception {
        mockMvc.perform(get("/api/weather/current"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}

