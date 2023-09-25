package com.example.csvspring.controller;

import com.example.csvspring.dto.AnalysisTemperature;
import com.example.csvspring.service.TemperatureService;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.text.DateFormatter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Component
@Slf4j
public class UiController implements Initializable {
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
    @FXML
    public Tab tabHdd;

    @FXML
    private TextField filePathField;

    @FXML
    private TableView<ObservableList<String>> tableView;

    @FXML
    private TableView<ObservableList<String>> tableViewHdd;

    @FXML
    private TableView<ObservableList<String>> tableViewCdd;

    @Autowired
    private TemperatureService temperatureService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        checkDoubleValue(fromDate);
        checkDoubleValue(toDate);
        checkDoubleValue(bp);
    }

    @FXML
    public void onBrowseButtonClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open CSV file");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV Files", "*.csv"), new FileChooser.ExtensionFilter("All Files", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            filePathField.setText(selectedFile.getAbsolutePath());
            saveButton.setDisable(false);
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

    private void populateHddAndCdd() {
        tableViewHdd.getColumns().clear();
        tableViewHdd.getItems().clear();
        createTableOfHddCdd(tableViewHdd, Double.parseDouble(bp.getText()), true);
        createTableOfHddCdd(tableViewCdd, Double.parseDouble(bp.getText()), false);
    }

    public void createTableOfHddCdd(TableView<ObservableList<String>> tableView, Double bp, Boolean isHdd) {
        // Sample data for AnalysisTemperature (replace with your data)
        List<AnalysisTemperature> data = temperatureService.getAnalysisTemperatures();
        List<List<String>> rows = new ArrayList<>();
        List<String> header = new ArrayList<>(List.copyOf(temperatureService.extractUniqueYears().stream().map(String::valueOf).toList()));
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
        tableView.getColumns().add(monthsColumn);
        for (Month month : map.keySet()) {
            List<String> row = new ArrayList<>();
            row.add(month.name());
            for (AnalysisTemperature analysisTemperature : map.get(month)) {
                Double val = isHdd ? temperatureService.getHdd(analysisTemperature.value(), bp) : temperatureService.getCdd(analysisTemperature.value(), bp);
                row.add(val.toString());
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
            tableView.getColumns().add(valueColumn);
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
        tableView.getColumns().add(avgColumn);
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
                    .mapToDouble(temperature -> isHdd ? temperatureService.getHdd(temperature.value(), bp) : temperatureService.getCdd(temperature.value(), bp))
                    .sum();
            totalRow.add(String.valueOf(sum));
        }
        rows.add(totalRow);


        // Add data to the table (skip the header row)
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            log.info("row: {}", row);
            ObservableList<String> observableRow = FXCollections.observableArrayList(row);
            tableView.getItems().add(observableRow);
        }
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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
                saveButton.setDisable(false);
                populateTable(file);
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }

    @FXML
    public void onSaveButtonClick() {
        temperatureService.saveData(filePathField.getText());
    }


    public void checkDoubleValue(TextField textField) {
        UnaryOperator<TextFormatter.Change> integerFormatter = change -> {
            try {
                if (change.getControlNewText().isEmpty()) {
                    return change;
                }
                double value = Double.parseDouble(change.getControlNewText());
                if (value < 0) {
                    textField.setStyle("-fx-text-fill: red;-fx-border-color: red;");
                    return null;
                }
            } catch (NumberFormatException e) {
                textField.setStyle("-fx-text-fill: red;-fx-border-color: red;");
                return null;
            }
            textField.setStyle("-fx-text-fill: green;-fx-border-color: green;");
            return change;
        };
        textField.setTextFormatter(new TextFormatter<>(integerFormatter));
    }

    public void onCalcButtonClick(ActionEvent actionEvent) {
        actionEvent.consume();
        //get this year from local date
        if (fromDate.getText().isEmpty() || toDate.getText().isEmpty() || bp.getText().isEmpty()) {
            createAlert("Please fill all the fields (from, to, bp)");
            return;
        } else {
            boolean isValideDateRange = Integer.parseInt(fromDate.getText()) < Integer.parseInt(toDate.getText())
                    && Integer.parseInt(fromDate.getText()) >= 1900
                    && Integer.parseInt(toDate.getText()) <= LocalDate.now().getYear();
            log.info("isValideDateRange: {}", isValideDateRange);
            if (!isValideDateRange) {
                log.info("from: {}, to: {}", fromDate.getText(), toDate.getText());
                createAlert("Please check your input (from, to) : from < to and from > 1900 and to < current year !!");
                return;
            } else if (Double.parseDouble(bp.getText()) < 0) {
                createAlert("Please check your input (bp) : bp > 0");
                return;
            }
        }
        temperatureService.calculateAvgOfAvgTemperatures(fromDate.getText(), toDate.getText());
        //switch tab
        SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
        selectionModel.select(tabHdd);

        populateHddAndCdd();
    }


    public void createAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
