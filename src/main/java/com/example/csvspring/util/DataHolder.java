package com.example.csvspring.util;

import com.example.csvspring.dto.AnalysisTemperature;
import lombok.Getter;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
@Getter
public class DataHolder implements DisposableBean {
    private final List<AnalysisTemperature> analysisTemperatures = new LinkedList<>();


    public void add(AnalysisTemperature analysisTemperature) {
        analysisTemperatures.add(analysisTemperature);
    }

    public void clear() {
        analysisTemperatures.clear();
    }

    @Override
    public void destroy() {
        clear();
    }
}
