package com.example.weather.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;

@Entity
public class WeatherObservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String city;
    private double temperature;
    private int humidity;
    private double windSpeed;
    private String condition;
    private LocalDateTime observedAt;
    private LocalDateTime createdAt;

    protected WeatherObservation() {
    }

    public WeatherObservation(
            String city,
            double temperature,
            int humidity,
            double windSpeed,
            String condition,
            LocalDateTime observedAt) {
        this.city = city;
        this.temperature = temperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.condition = condition;
        this.observedAt = observedAt;
    }

    @PrePersist
    public void setCreatedAtBeforeSaving() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getCity() {
        return city;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public String getCondition() {
        return condition;
    }

    public LocalDateTime getObservedAt() {
        return observedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

