package com.example.weather.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.weather.model.WeatherObservation;

public interface WeatherObservationRepository extends JpaRepository<WeatherObservation, Long> {

    List<WeatherObservation> findByCityIgnoreCaseOrderByCreatedAtDesc(String city, Pageable pageable);
}

