package com.example.weather.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.weather.client.WeatherApiClient;
import com.example.weather.dto.AverageTemperatureResponse;
import com.example.weather.dto.CurrentWeatherResponse;
import com.example.weather.dto.ForecastResponse;
import com.example.weather.dto.GeocodingApiResponse.LocationResult;
import com.example.weather.dto.HottestHourResponse;
import com.example.weather.dto.HourlyForecast;
import com.example.weather.dto.UmbrellaResponse;
import com.example.weather.dto.WeatherApiResponse;
import com.example.weather.dto.WeatherApiResponse.CurrentWeather;
import com.example.weather.dto.WeatherApiResponse.HourlyWeather;
import com.example.weather.dto.WeatherObservationResponse;
import com.example.weather.model.WeatherObservation;
import com.example.weather.repository.WeatherObservationRepository;

@Service
public class WeatherService {

    public static final int RAIN_PROBABILITY_THRESHOLD = 50;

    private final WeatherApiClient weatherApiClient;
    private final WeatherObservationRepository observationRepository;

    public WeatherService(
            WeatherApiClient weatherApiClient,
            WeatherObservationRepository observationRepository) {
        this.weatherApiClient = weatherApiClient;
        this.observationRepository = observationRepository;
    }

    public CurrentWeatherResponse getCurrentWeather(String city) {
        LocationResult location = findLocation(city);
        WeatherApiResponse apiResponse = weatherApiClient.getCurrentWeather(
                location.latitude(), location.longitude());

        if (apiResponse == null || apiResponse.current() == null) {
            throw incompleteWeatherResponse();
        }

        CurrentWeather current = apiResponse.current();
        CurrentWeatherResponse response = new CurrentWeatherResponse(
                location.name(),
                current.temperature(),
                current.humidity(),
                current.windSpeed(),
                describeWeatherCode(current.weatherCode()),
                LocalDateTime.parse(current.time()));

        observationRepository.save(new WeatherObservation(
                response.city(),
                response.temperature(),
                response.humidity(),
                response.windSpeed(),
                response.condition(),
                response.observedAt()));

        return response;
    }

    public ForecastResponse getForecast(String city) {
        LocationResult location = findLocation(city);
        WeatherApiResponse apiResponse = weatherApiClient.getForecast(
                location.latitude(), location.longitude());

        if (apiResponse == null || apiResponse.hourly() == null) {
            throw incompleteWeatherResponse();
        }

        return new ForecastResponse(location.name(), toHourlyForecasts(apiResponse.hourly()));
    }

    public AverageTemperatureResponse getAverageTemperature(String city) {
        ForecastResponse forecast = getForecast(city);
        return new AverageTemperatureResponse(
                forecast.city(), calculateAverageTemperature(forecast.hourly()));
    }

    public HottestHourResponse getHottestHour(String city) {
        ForecastResponse forecast = getForecast(city);
        HourlyForecast hottest = findHottestHour(forecast.hourly());
        return new HottestHourResponse(forecast.city(), hottest.time(), hottest.temperature());
    }

    public UmbrellaResponse getUmbrellaRecommendation(String city) {
        ForecastResponse forecast = getForecast(city);
        boolean umbrellaRecommended = isUmbrellaRecommended(forecast.hourly());
        String message = umbrellaRecommended
                ? "Rain is expected in the next 12 hours. Bring an umbrella!"
                : "No significant rain expected in the next 12 hours.";

        return new UmbrellaResponse(forecast.city(), umbrellaRecommended, message);
    }

    public List<WeatherObservationResponse> getHistory(String city, int limit) {
        validateCity(city);
        if (limit < 1 || limit > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Limit must be between 1 and 100");
        }

        return observationRepository
                .findByCityIgnoreCaseOrderByCreatedAtDesc(city.trim(), PageRequest.of(0, limit))
                .stream()
                .map(this::toObservationResponse)
                .toList();
    }

    public double calculateAverageTemperature(List<HourlyForecast> hourlyForecasts) {
        requireForecastData(hourlyForecasts);
        double average = hourlyForecasts.stream()
                .mapToDouble(HourlyForecast::temperature)
                .average()
                .orElseThrow();
        return Math.round(average * 10.0) / 10.0;
    }

    public HourlyForecast findHottestHour(List<HourlyForecast> hourlyForecasts) {
        requireForecastData(hourlyForecasts);
        return hourlyForecasts.stream()
                .max(Comparator.comparingDouble(HourlyForecast::temperature))
                .orElseThrow();
    }

    public boolean isUmbrellaRecommended(List<HourlyForecast> hourlyForecasts) {
        requireForecastData(hourlyForecasts);
        return hourlyForecasts.stream()
                .anyMatch(hour -> hour.rainProbability() >= RAIN_PROBABILITY_THRESHOLD);
    }

    private LocationResult findLocation(String city) {
        validateCity(city);
        return weatherApiClient.findLocation(city.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "City not found: " + city));
    }

    private List<HourlyForecast> toHourlyForecasts(HourlyWeather hourly) {
        if (hourly.time() == null
                || hourly.temperatures() == null
                || hourly.rainProbabilities() == null
                || hourly.weatherCodes() == null
                || hourly.time().isEmpty()
                || hourly.time().size() != hourly.temperatures().size()
                || hourly.time().size() != hourly.rainProbabilities().size()
                || hourly.time().size() != hourly.weatherCodes().size()) {
            throw incompleteWeatherResponse();
        }

        return IntStream.range(0, hourly.time().size())
                .mapToObj(index -> new HourlyForecast(
                        LocalDateTime.parse(hourly.time().get(index)),
                        hourly.temperatures().get(index),
                        hourly.rainProbabilities().get(index),
                        describeWeatherCode(hourly.weatherCodes().get(index))))
                .toList();
    }

    private WeatherObservationResponse toObservationResponse(WeatherObservation observation) {
        return new WeatherObservationResponse(
                observation.getId(),
                observation.getCity(),
                observation.getTemperature(),
                observation.getHumidity(),
                observation.getWindSpeed(),
                observation.getCondition(),
                observation.getObservedAt(),
                observation.getCreatedAt());
    }

    private void validateCity(String city) {
        if (city == null || city.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "City must not be blank");
        }
    }

    private void requireForecastData(List<HourlyForecast> hourlyForecasts) {
        if (hourlyForecasts == null || hourlyForecasts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Forecast is empty");
        }
    }

    private ResponseStatusException incompleteWeatherResponse() {
        return new ResponseStatusException(
                HttpStatus.BAD_GATEWAY, "Weather service returned an incomplete response");
    }

    private String describeWeatherCode(int weatherCode) {
        return switch (weatherCode) {
            case 0 -> "Clear Sky";
            case 1, 2 -> "Partly Cloudy";
            case 3 -> "Overcast";
            case 45, 48 -> "Fog";
            case 51, 53, 55, 56, 57 -> "Drizzle";
            case 61, 63, 65, 66, 67, 80, 81, 82 -> "Rain";
            case 71, 73, 75, 77, 85, 86 -> "Snow";
            case 95, 96, 99 -> "Thunderstorm";
            default -> "Unknown";
        };
    }
}
