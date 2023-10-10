package com.example.csvspring;

import com.example.csvspring.fx.JavaFxApplication;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CsvSpringApplication {

    public static void main(String[] args) {
        Application.launch(JavaFxApplication.class, args);
    }

}
