package com.example.csvspring.controller;

import com.example.csvspring.model.Temperature;
import com.example.csvspring.service.JobService;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class UiController {

    @FXML
    public Button saveButton;
    @Autowired
    private JobService jobService;
    @Autowired
    private Job job;
    @FXML
    private TextField filePathField;
    @FXML
    private TableView<ObservableList<String>> tableView;

    private File csvFile;

    private List<Temperature> temperatures = new LinkedList<>();

    @FXML
    public void onBrowseButtonClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open CSV file");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV Files", "*.csv"), new FileChooser.ExtensionFilter("All Files", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(null);
        csvFile = selectedFile;
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

                for (int columnIndex = 0; columnIndex < numColumns; columnIndex++) {
                    if (columnIndex == 0) {
                        TableColumn<ObservableList<String>, String> column = new TableColumn<>("Station_ID_code");
                        final int colIndex = columnIndex;

                        column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(colIndex)));

                        tableView.getColumns().add(column);
                    } else if (columnIndex == 1) {
                        TableColumn<ObservableList<String>, String> column = new TableColumn<>("Weather_ID_code");
                        final int colIndex = columnIndex;

                        column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(colIndex)));

                        tableView.getColumns().add(column);
                    } else if (columnIndex == 2) {
                        TableColumn<ObservableList<String>, String> column = new TableColumn<>("Station_SiteName");
                        final int colIndex = columnIndex;

                        column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(colIndex)));

                        tableView.getColumns().add(column);
                    } else if (columnIndex == 3) {
                        TableColumn<ObservableList<String>, String> column = new TableColumn<>("Year");
                        final int colIndex = columnIndex;

                        column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(colIndex)));

                        tableView.getColumns().add(column);
                    } else if (columnIndex == 4) {
                        TableColumn<ObservableList<String>, String> column = new TableColumn<>("Month");
                        final int colIndex = columnIndex;

                        column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(colIndex)));

                        tableView.getColumns().add(column);
                    } else {
                        int i = columnIndex - 4;
                        if (i % 2 != 0) {
                            TableColumn<ObservableList<String>, String> columnHigh = new TableColumn<>("High" + i);
                            final int colIndexHigh = columnIndex;

                            columnHigh.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(colIndexHigh)));

                            tableView.getColumns().add(columnHigh);
                        } else {

                            TableColumn<ObservableList<String>, String> columnLow = new TableColumn<>("Low" + i);
                            final int colIndexLow = columnIndex;

                            columnLow.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(colIndexLow)));

                            tableView.getColumns().add(columnLow);
                        }
                    }
                }

                // Add data to the table
                for (String[] row : rows) {
                    ObservableList<String> observableRow = FXCollections.observableArrayList(row);
                    tableView.getItems().add(observableRow);
                }
            }
        }
    }

    // Method to get all data from the TableView
    private void getAllDataFromTableView() {
        for (ObservableList<String> row : tableView.getItems()) {
            Temperature temperature = new Temperature();
            for (int i = 5; i < row.size(); i++) {
                temperature.setStationId(Long.parseLong(row.get(0)));
                temperature.setWhetherId(row.get(1));
                temperature.setStationName(row.get(2));
                temperature.setYear(Long.parseLong(row.get(3)));
                temperature.setMonth(Long.parseLong(row.get(4)));

                if (i % 2 != 0) {
                    if (!row.get(i).isEmpty() && !row.get(i).equals(" ")) {
                        temperature.getHighTemperatures().add(Double.parseDouble(row.get(i)));
                    }
                } else {
                    if (!row.get(i).isEmpty() && !row.get(i).equals(" ")) {
                        temperature.getLowTemperatures().add(Double.parseDouble(row.get(i)));
                    }
                }
            }
            temperatures.add(temperature);
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

    private <T> TableColumn<T, ?> getTableColumnByName(TableView<T> tableView, String name, int index) {
        for (TableColumn<T, ?> col : tableView.getColumns())
            if (col.getText().equalsIgnoreCase(name))
                return col.getColumns().get(index);
        return null;
    }

    public void saveData() {
        JobParameter<?> jobParameter = new JobParameter<>(filePathField.getText(), String.class);
        Map<String, JobParameter<?>> params = new HashMap<>();
        params.put("file", jobParameter);
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

        // this.getAllDataFromTableView();
        //temperatures.forEach(temperature -> log.info(temperature.toString()));
    }

    @FXML
    public void onSaveButtonClick(ActionEvent actionEvent) {
        saveData();
    }
}
