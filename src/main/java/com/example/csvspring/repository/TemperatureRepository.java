package com.example.csvspring.repository;

import com.example.csvspring.model.Temperature;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemperatureRepository extends JpaRepository<Temperature, Long> {
}