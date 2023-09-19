package com.example.csvspring.controller;

import com.example.csvspring.model.Temperature;
import com.example.csvspring.service.JobService;
import com.example.csvspring.util.DatePickerConverter;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Component
@Slf4j
public class UiController {

    @Autowired
    private JobService jobService;
    @Autowired
    private Job job;
    @Autowired
    private DatePickerConverter converter;
    @FXML
    public  TextField bp;
    @FXML
    public Button saveButton;
    @FXML
    public Button calculHDD;
    @FXML
    public TextField fromDate;
    @FXML
    public TextField toDate;
    @FXML
    public TabPane tabPane;
    @FXML
    private TextField filePathField;
    @FXML
    private TableView<ObservableList<String>> tableView;
    @FXML
    private TableView tableViewHddCdd;


    @FXML
    public void onBrowseButtonClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open CSV file");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV Files", "*.csv"), new FileChooser.ExtensionFilter("All Files", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            filePathField.setText(selectedFile.getAbsolutePath());
            try {
                populateTable(selectedFile);
            } catch (IOException | CsvException ioException) {
                log.error(ioException.getMessage(), ioException);
            }
        }
    }


    private void populateTable(File csvFile) throws IOException, CsvException {
        tableView.getColumns().clear();
        tableView.getItems().clear();

        try (CSVReader csvReader = new CSVReader(new FileReader(csvFile))) {
            List<String[]> rows = csvReader.readAll();

            if (!rows.isEmpty()) {
                int numColumns = rows.get(0).length;
                int numDays = (numColumns - 5) / 2; // Calculate the number of days

                // Add columns for Station_ID_code, Weather_ID_code, Station_SiteName, Year, and Month
                for (int columnIndex = 0; columnIndex < 5; columnIndex++) {
                    switch (columnIndex) {
                        case 0 -> {
                            TableColumn<ObservableList<String>, String> column = new TableColumn<>("Station_ID_code");
                            final int colIndex = columnIndex;

                            column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(colIndex)));

                            tableView.getColumns().add(column);
                        }
                        case 1 -> {
                            TableColumn<ObservableList<String>, String> column = new TableColumn<>("Weather_ID_code");
                            final int colIndex = columnIndex;

                            column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(colIndex)));

                            tableView.getColumns().add(column);
                        }
                        case 2 -> {
                            TableColumn<ObservableList<String>, String> column = new TableColumn<>("Station_SiteName");
                            final int colIndex = columnIndex;

                            column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(colIndex)));

                            tableView.getColumns().add(column);
                        }
                        case 3 -> {
                            TableColumn<ObservableList<String>, String> column = new TableColumn<>("Year");
                            final int colIndex = columnIndex;

                            column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(colIndex)));

                            tableView.getColumns().add(column);
                        }
                        default -> {
                            TableColumn<ObservableList<String>, String> column = new TableColumn<>("Month");
                            final int colIndex = columnIndex;

                            column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(colIndex)));

                            tableView.getColumns().add(column);
                        }
                    }
                }

                // Add columns for High and Low temperatures for each day
                for (int day = 1; day <= numDays; day++) {
                    TableColumn<ObservableList<String>, String> columnHigh = new TableColumn<>("High" + day);
                    final int colIndexHigh = 4 + (day - 1) * 2; // Calculate the index for High temperature

                    columnHigh.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(colIndexHigh)));
                    tableView.getColumns().add(columnHigh);

                    TableColumn<ObservableList<String>, String> columnLow = new TableColumn<>("Low" + day);
                    final int colIndexLow = colIndexHigh + 1; // Calculate the index for Low temperature

                    columnLow.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(colIndexLow)));
                    tableView.getColumns().add(columnLow);
                }

                // Add data to the table (skip the header row)
                for (String[] row : rows) {
                    ObservableList<String> observableRow = FXCollections.observableArrayList(row);
                    tableView.getItems().add(observableRow);
                }
            }
        }
    }


    public void handleDragOver(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
            boolean isAccepted = db.getFiles().stream().allMatch(file -> {
                String fileName = file.getName();
                String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                return extension.equals("csv");
            });
            if (isAccepted) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
        }
        event.consume();
    }

    public void handleDragDropped(DragEvent event) throws IOException, CsvException {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            success = true;
            for (File file : db.getFiles()) {
                filePathField.setText(file.getAbsolutePath());
                populateTable(file);
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }


    public void saveData() {
        JobParameter<?> jobParameter = new JobParameter<>(filePathField.getText(), String.class);
        Map<String, JobParameter<?>> params = new HashMap<>();
        params.put("file", jobParameter);
        params.put("id", new JobParameter<>(UUID.randomUUID().toString(),String.class));
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

    @FXML
    public void onSaveButtonClick() {
        saveData();
    }

    @FXML
    public void onBPChange(KeyEvent actionEvent) {
        actionEvent.consume();
        bp.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isValid(newValue) || newValue.isEmpty()) {
                // Input is not valid, display an error
                bp.setStyle("-fx-border-color: red;");
                calculHDD.setDisable(true);
            } else {
                // Input is valid, clear any error styling
                bp.setStyle(null);
                calculHDD.setDisable(false);
            }
        });
    }

    private boolean isValid(String input) {
        // Implement your validation logic here
        try {
            double value = Double.parseDouble(input);
            // Example: Validate that the input is a positive number
            return value > 0;
        } catch (NumberFormatException e) {
            // Input is not a valid number
            return false;
        }
    }

    public void onCalcButtonClick(ActionEvent actionEvent) {
    }

    public void onDateChange(ActionEvent actionEvent) {
        actionEvent.consume();
        calculHDD.setDisable(fromDate.getText() == null || toDate.getText() == null);
    }
}
