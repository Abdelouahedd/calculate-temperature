package com.example.csvspring.repository;

import com.example.csvspring.model.Temperature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TemperatureRepository extends JpaRepository<Temperature, Long> {
    //SELECT distibct city FROM temperature
    @Query("SELECT DISTINCT t.stationName FROM Temperature t")
    List<String>getCities();
}