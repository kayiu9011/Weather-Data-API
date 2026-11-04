package com.example.weather.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import com.example.weather.client.WeatherApiClient;
import com.example.weather.dto.CurrentWeatherResponse;
import com.example.weather.dto.GeocodingApiResponse.LocationResult;
import com.example.weather.dto.HourlyForecast;
import com.example.weather.dto.WeatherApiResponse;
import com.example.weather.dto.WeatherApiResponse.CurrentWeather;
import com.example.weather.model.WeatherObservation;
import com.example.weather.repository.WeatherObservationRepository;

class WeatherServiceTests {

    private final WeatherApiClient weatherApiClient = mock(WeatherApiClient.class);
    private final WeatherObservationRepository observationRepository =
            mock(WeatherObservationRepository.class);
    private final WeatherService weatherService =
            new WeatherService(weatherApiClient, observationRepository);

    @Test
    void transformsAndSavesCurrentWeather() {
        LocationResult singapore = new LocationResult("Singapore", 1.2897, 103.8501);
        CurrentWeather current = new CurrentWeather("2026-11-04T12:00", 30.2, 74, 2, 8.4);

        when(weatherApiClient.findLocation("Singapore")).thenReturn(Optional.of(singapore));
        when(weatherApiClient.getCurrentWeather(1.2897, 103.8501))
                .thenReturn(new WeatherApiResponse(current, null));

        CurrentWeatherResponse response = weatherService.getCurrentWeather("Singapore");

        assertThat(response.city()).isEqualTo("Singapore");
        assertThat(response.temperature()).isEqualTo(30.2);
        assertThat(response.humidity()).isEqualTo(74);
        assertThat(response.windSpeed()).isEqualTo(8.4);
        assertThat(response.condition()).isEqualTo("Partly Cloudy");
        assertThat(response.observedAt()).hasToString("2026-11-04T12:00");
        verify(observationRepository).save(any(WeatherObservation.class));
    }

    @Test
    void calculatesAverageTemperature() {
        double average = weatherService.calculateAverageTemperature(List.of(
                forecastAt(12, 30, 10),
                forecastAt(13, 31, 10),
                forecastAt(14, 29, 10),
                forecastAt(15, 32, 10),
                forecastAt(16, 28, 10)));

        assertThat(average).isEqualTo(30.0);
    }

    @Test
    void findsHottestForecastHour() {
        HourlyForecast hottest = weatherService.findHottestHour(List.of(
                forecastAt(12, 30, 10),
                forecastAt(13, 31, 10),
                forecastAt(14, 32.4, 10),
                forecastAt(15, 29, 10)));

        assertThat(hottest.time()).isEqualTo(LocalDateTime.of(2026, 11, 4, 14, 0));
        assertThat(hottest.temperature()).isEqualTo(32.4);
    }

    @Test
    void recommendsUmbrellaWhenThresholdIsReached() {
        boolean recommended = weatherService.isUmbrellaRecommended(List.of(
                forecastAt(12, 30, 20),
                forecastAt(13, 30, WeatherService.RAIN_PROBABILITY_THRESHOLD)));

        assertThat(recommended).isTrue();
    }

    @Test
    void doesNotRecommendUmbrellaBelowThreshold() {
        boolean recommended = weatherService.isUmbrellaRecommended(List.of(
                forecastAt(12, 30, 20),
                forecastAt(13, 30, 49)));

        assertThat(recommended).isFalse();
    }

    @Test
    void rejectsEmptyForecastData() {
        assertThatThrownBy(() -> weatherService.calculateAverageTemperature(List.of()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Forecast is empty");
    }

    private HourlyForecast forecastAt(int hour, double temperature, int rainProbability) {
        return new HourlyForecast(
                LocalDateTime.of(2026, 11, 4, hour, 0),
                temperature,
                rainProbability,
                "Partly Cloudy");
    }
}
