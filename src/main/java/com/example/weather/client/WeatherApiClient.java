package com.example.weather.client;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Optional;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.example.weather.dto.GeocodingApiResponse;
import com.example.weather.dto.GeocodingApiResponse.LocationResult;
import com.example.weather.dto.WeatherApiResponse;

@Component
public class WeatherApiClient {

    private final RestClient geocodingClient;
    private final RestClient weatherClient;

    public WeatherApiClient(RestClient.Builder restClientBuilder) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        this.geocodingClient = restClientBuilder.clone()
                .baseUrl("https://geocoding-api.open-meteo.com")
                .requestFactory(requestFactory)
                .build();
        this.weatherClient = restClientBuilder.clone()
                .baseUrl("https://api.open-meteo.com")
                .requestFactory(requestFactory)
                .build();
    }

    public Optional<LocationResult> findLocation(String city) {
        GeocodingApiResponse response = geocodingClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/search")
                        .queryParam("name", city)
                        .queryParam("count", 1)
                        .queryParam("language", "en")
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .body(GeocodingApiResponse.class);

        if (response == null || response.results() == null || response.results().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(response.results().getFirst());
    }

    public WeatherApiResponse getCurrentWeather(double latitude, double longitude) {
        return weatherClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/forecast")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("current",
                                "temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m")
                        .queryParam("timezone", "auto")
                        .build())
                .retrieve()
                .body(WeatherApiResponse.class);
    }

    public WeatherApiResponse getForecast(double latitude, double longitude) {
        return weatherClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/forecast")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("hourly",
                                "temperature_2m,precipitation_probability,weather_code")
                        .queryParam("forecast_hours", 12)
                        .queryParam("timezone", "auto")
                        .build())
                .retrieve()
                .body(WeatherApiResponse.class);
    }
}

