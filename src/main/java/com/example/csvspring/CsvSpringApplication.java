package com.example.csvspring;

import javafx.application.Application;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CsvSpringApplication {

    public static void main(String[] args) {
        Application.launch(JavaFxApplication.class, args);
    }

}
