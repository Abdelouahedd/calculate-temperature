package com.example.csvspring.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Popup;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class YearPickerController {
    public TextField yearTextField;
    private Popup yearPopup;

    public YearPickerController() {
        yearPopup = createYearPopup();
    }

    @FXML
    private void onYearTextFieldAction() {
        // Show the year popup when the user interacts with the text field
        yearPopup.show(yearTextField.getScene().getWindow());
    }

    private Popup createYearPopup() {
        Popup popup = new Popup();
        popup.setAutoHide(true);

        DatePicker yearPicker = new DatePicker();
        yearPicker.setConverter(new StringConverter<>() {
            private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy");

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        });

        yearPicker.setOnAction(event -> {
            yearTextField.setText(yearPicker.getEditor().getText());
            popup.hide();
        });

        popup.getContent().add(yearPicker);

        return popup;
    }
}
