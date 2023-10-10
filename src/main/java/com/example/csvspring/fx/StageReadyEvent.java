package com.example.csvspring.fx;

import javafx.stage.Stage;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class StageReadyEvent extends ApplicationEvent {
    public StageReadyEvent(Stage stage) {
        super(stage);
    }

    public Stage getStage() {
        return (Stage) getSource();
    }
}
