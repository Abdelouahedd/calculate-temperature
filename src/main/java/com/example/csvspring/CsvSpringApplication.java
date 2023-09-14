package com.example.csvspring;

import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableMongoRepositories
public class CsvSpringApplication {

    public static void main(String[] args) {
       // SpringApplication.run(CsvSpringApplication.class, args);
        Application.launch(JavaFxApplication.class, args);
    }

}
