package com.example.csvspring;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;

@Component
public class StageListener implements ApplicationListener<StageReadyEvent> {

    private final Resource propertySource;
    private final ApplicationContext applicationContext;

    public StageListener(
            @Value("classpath:/ui.fxml")
            Resource propertySource,
            ApplicationContext applicationContext) {
        this.propertySource = propertySource;
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        try {
            Stage stage = event.getStage();
            URL ui = propertySource.getURL();
            FXMLLoader fxmlLoader = new FXMLLoader(ui);
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Parent root = fxmlLoader.load();
            stage.setScene(new javafx.scene.Scene(root, 800, 600));
            //css
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
