package com.example.csvspring.controller;

import com.example.csvspring.dto.AnalysisTemperature;
import com.example.csvspring.service.JobService;
import com.example.csvspring.util.DataHolder;
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
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class UiController {
    @FXML
    public TextField bp;
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
    public Tab tabHdd;

    @FXML
    private TextField filePathField;

    @FXML
    private TableView<ObservableList<String>> tableView;

    @FXML
    private TableView<ObservableList<String>> tableViewHddCdd;

    @Autowired
    private JobService jobService;

    @Autowired
    private Job job;

    @Autowired
    private DataHolder dataHolder;

    @Autowired
    @Qualifier("calculate_job")
    private Job jobHddCdd;


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

    private void populateHdd() {
        tableViewHddCdd.getColumns().clear();
        tableViewHddCdd.getItems().clear();
        createTableOfHddCdd();
    }

    public void createTableOfHddCdd() {
        // Sample data for AnalysisTemperature (replace with your data)
        List<AnalysisTemperature> data = dataHolder.getAnalysisTemperatures();
        List<List<String>> rows = new ArrayList<>();
        List<String> header = new ArrayList<>(List.copyOf(extractUniqueYears().stream().map(String::valueOf).toList()));
        header.add(0, "Months");
        rows.add(header);
        // Group by month
        Map<Month, List<AnalysisTemperature>> map = data.stream().collect(Collectors.groupingBy(AnalysisTemperature::month));
        // Order map by month
        map = map.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        // Create the "Months" column
        TableColumn<ObservableList<String>, String> monthsColumn = new TableColumn<>("Months");
        monthsColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().get(0)));
        tableViewHddCdd.getColumns().add(monthsColumn);
        for (Month month : map.keySet()) {
            List<String> row = new ArrayList<>();
            row.add(month.name());
            for (AnalysisTemperature analysisTemperature : map.get(month)) {
                row.add(analysisTemperature.value().toString());
            }
            rows.add(row);
        }
        // Create columns for the values
        for (int i = 1; i < rows.get(0).size(); i++) {
            log.info("header: {}", rows.get(0).get(i));
            final int columnIndex = i; // Capture the column index
            TableColumn<ObservableList<String>, String> valueColumn = new TableColumn<>(rows.get(0).get(i));
            valueColumn.setCellValueFactory(cellData -> {
                List<String> values = cellData.getValue();
                if (columnIndex < values.size()) {
                    return new SimpleStringProperty(String.format("%.2f", Double.parseDouble(values.get(columnIndex))));
                }
                return new SimpleStringProperty("");
            });
            tableViewHddCdd.getColumns().add(valueColumn);
        }
        //create avg column
        TableColumn<ObservableList<String>, String> avgColumn = new TableColumn<>("Avg");
        avgColumn.setCellValueFactory(cellData -> {
            List<String> values = cellData.getValue();
            double avg = values.stream()
                    .skip(1)
                    .mapToDouble(Double::parseDouble)
                    .average()
                    .orElse(Double.parseDouble("0"));
            return new SimpleStringProperty(String.format("%.2f", avg));
        });
        tableViewHddCdd.getColumns().add(avgColumn);
        //totol of value by year
        //group by year
        Map<Long, List<AnalysisTemperature>> mapYear = data.stream().collect(Collectors.groupingBy(AnalysisTemperature::year));
        // Order map by year
        mapYear = mapYear.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        List<String> totalRow = new ArrayList<>();
        totalRow.add("Total");
        for (Long year : mapYear.keySet()) {
            double sum = mapYear.get(year).stream()
                    .map(AnalysisTemperature::value)
                    .mapToDouble(Double::doubleValue)
                    .sum();
            totalRow.add(String.valueOf(sum));
        }
        rows.add(totalRow);


        // Add data to the table (skip the header row)
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            log.info("row: {}", row);
            ObservableList<String> observableRow = FXCollections.observableArrayList(row);
            tableViewHddCdd.getItems().add(observableRow);
        }
        tableViewHddCdd.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }


    private List<Long> extractUniqueYears() {
        Set<Long> yearsSet = new HashSet<>();
        for (AnalysisTemperature analysisTemperature : dataHolder.getAnalysisTemperatures()) {
            Long year = analysisTemperature.year();
            yearsSet.add(year);
        }
        List<Long> years = new ArrayList<>(yearsSet);
        Collections.sort(years);
        return years;
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

    public void onCalcButtonClick(ActionEvent actionEvent) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        actionEvent.consume();
        log.info("Calculating HDD/CDD for the period from {} to {} with base point {}", fromDate.getText(), toDate.getText(), bp.getText());
        JobParameter<?> from = new JobParameter<>(fromDate.getText(), String.class);
        JobParameter<?> to = new JobParameter<>(toDate.getText(), String.class);
        JobParameter<?> bp = new JobParameter<>(this.bp.getText(), String.class);
        JobParameter<String> uuid = new JobParameter<>(UUID.randomUUID().toString(), String.class);
        JobParameters jobParameters = new JobParameters(Map.of("from", from, "to", to, "bp", bp, "id", uuid));

        jobService.launchJob(jobHddCdd, jobParameters);
        //switch tab
        SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
        selectionModel.select(tabHdd);

        populateHdd();
    }

    public void onDateChange(ActionEvent actionEvent) {
        actionEvent.consume();
        calculHDD.setDisable(fromDate.getText() == null || toDate.getText() == null);
        log.info("fromDate: {}, toDate: {}", fromDate.getText(), toDate.getText());
    }
}
