package com.example.csvspring.dto;

import java.time.Month;

public record AnalysisTemperature(Long year, Month month, Double value) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnalysisTemperature that)) return false;
        return (year.equals(that.year) && month.equals(that.month));
    }
}
