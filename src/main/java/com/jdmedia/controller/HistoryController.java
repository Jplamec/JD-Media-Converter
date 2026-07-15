package com.jdmedia.controller;

import com.jdmedia.model.ConversionRecord;
import com.jdmedia.service.AppContext;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class HistoryController {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final AppContext context;
    @FXML private TableView<ConversionRecord> table;
    @FXML private TableColumn<ConversionRecord, String> sourceColumn, resultColumn, dateColumn;
    @FXML private TableColumn<ConversionRecord, ConversionRecord> pathColumn;

    public HistoryController(AppContext context) { this.context = context; }

    @FXML public void initialize() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        sourceColumn.setCellValueFactory(value -> new javafx.beans.property.SimpleStringProperty(Path.of(value.getValue().source()).getFileName().toString()));
        resultColumn.setCellValueFactory(value -> new javafx.beans.property.SimpleStringProperty(value.getValue().result()));
        dateColumn.setCellValueFactory(value -> new javafx.beans.property.SimpleStringProperty(DATE.format(value.getValue().startedAt().atZone(ZoneId.systemDefault()))));
        center(dateColumn); center(sourceColumn); center(resultColumn);
        pathColumn.setCellValueFactory(value -> new javafx.beans.property.SimpleObjectProperty<>(value.getValue()));
        pathColumn.setCellFactory(column -> new TableCell<>() {
            private final Button button = new Button("Abrir carpeta");
            { button.getStyleClass().add("table-action"); button.setOnAction(event -> openFolder(getItem())); }
            @Override protected void updateItem(ConversionRecord item, boolean empty) { super.updateItem(item, empty); setGraphic(empty ? null : button); setAlignment(javafx.geometry.Pos.CENTER); }
        });
        table.getItems().setAll(context.history().recent());
    }

    private void center(TableColumn<ConversionRecord, String> column) { column.setStyle("-fx-alignment: CENTER;"); }
    @FXML private void home() { context.navigation().show("home-view.fxml"); }
    private void openFolder(ConversionRecord record) {
        try {
            String output = record.output();
            Path folder = output == null || output.isBlank() ? Path.of(record.source()).getParent() : Path.of(output).getParent();
            Desktop.getDesktop().open(folder.toFile());
        } catch (IOException exception) { context.status().error("No se pudo abrir la carpeta: " + exception.getMessage()); }
    }
}
