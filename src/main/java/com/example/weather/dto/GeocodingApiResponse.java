package com.example.weather.dto;

import java.util.List;

public record GeocodingApiResponse(List<LocationResult> results) {

    public record LocationResult(String name, double latitude, double longitude) {
    }
}

