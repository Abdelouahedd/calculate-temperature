package com.example.csvspring.service;

import com.example.csvspring.dto.AnalysisTemperature;
import com.example.csvspring.util.DataHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class TemperatureService {
    private final JobService jobService;
    private final Job job;
    private final DataHolder dataHolder;
    private final Job jobHddCdd;

    public TemperatureService(JobService jobService,
                              DataHolder dataHolder,
                              Job job,
                              @Qualifier("calculate_job") Job jobHddCdd) {
        this.jobService = jobService;
        this.job = job;
        this.dataHolder = dataHolder;
        this.jobHddCdd = jobHddCdd;
    }

    public Double getHdd(Double avg, Double bp) {
        return Math.max(bp - avg, 0);
    }

    public Double getCdd(Double avg, Double bp) {
        return Math.max(avg - bp, 0);
    }

    public List<Long> extractUniqueYears() {
        Set<Long> yearsSet = new HashSet<>();
        for (AnalysisTemperature analysisTemperature : dataHolder.getAnalysisTemperatures()) {
            Long year = analysisTemperature.year();
            yearsSet.add(year);
        }
        List<Long> years = new ArrayList<>(yearsSet);
        Collections.sort(years);
        return years;
    }

    public void saveData(String path) {
        if (path == null || path.isEmpty()) {
            log.error("Path of the file is empty");
            return;
        }
        JobParameter<?> jobParameter = new JobParameter<>(path, String.class);
        Map<String, JobParameter<?>> params = new HashMap<>();
        params.put("file", jobParameter);
        params.put("id", new JobParameter<>(UUID.randomUUID().toString(), String.class));
        JobParameters jobParameters = new JobParameters(params);
        try {
            JobExecution jobExecution = jobService.launchJob(job, jobParameters);
            if (jobExecution.getStatus().equals(BatchStatus.COMPLETED)) {
                log.info("Job completed successfully");
            } else {
                log.info("Job failed with following status {}", jobExecution.getStatus());
            }
        } catch (JobParametersInvalidException | JobRestartException | JobInstanceAlreadyCompleteException |
                 JobExecutionAlreadyRunningException e) {
            log.error(e.getMessage(), e);
        }
    }


    public void calculateAvgOfAvgTemperatures(String fromDate, String toDate) {
        if (fromDate == null || fromDate.isEmpty() || toDate == null || toDate.isEmpty()) {
            log.error("From and to date is empty");
            return;
        }
        JobParameter<?> uuid = new JobParameter<>(UUID.randomUUID().toString(), String.class);
        JobParameter<?> from = new JobParameter<>(fromDate, String.class);
        JobParameter<?> to = new JobParameter<>(toDate, String.class);
        JobParameters jobParameters = new JobParameters(Map.of("from", from, "to", to, "id", uuid));
        try {
            JobExecution jobExecution = jobService.launchJob(jobHddCdd, jobParameters);
            if (jobExecution.getStatus().equals(BatchStatus.COMPLETED)) {
                log.info("Job calculate completed successfully");
            } else {
                log.info("Job calculate failed with following status {}", jobExecution.getStatus());
            }
        } catch (JobParametersInvalidException | JobRestartException | JobInstanceAlreadyCompleteException |
                 JobExecutionAlreadyRunningException e) {
            log.error(e.getMessage(), e);
        }
    }

    public List<AnalysisTemperature> getAnalysisTemperatures() {
        return dataHolder.getAnalysisTemperatures();
    }
}
