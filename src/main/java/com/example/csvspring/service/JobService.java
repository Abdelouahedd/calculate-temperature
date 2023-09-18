package com.example.csvspring.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.*;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.stereotype.Service;

import java.io.File;


@Log4j2
@Service
public record JobService(JobLauncher jobLauncher) {


    public JobExecution launchJob(Job job, JobParameters jobParameters) throws JobParametersInvalidException, JobRestartException, JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException {
        return jobLauncher.run(job, jobParameters);
    }

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        JobParameters jobParameters = stepExecution.getJobParameters();

        JobParameter<?> file = jobParameters.getParameters().get("file");
        if (file != null) {
            File csvFile = (File) file.getValue();
            log.info("------------------------------> File: {}", csvFile.getAbsolutePath());
        } else {
            log.info("------------------------------> File is null");
        }

    }

}
