package com.jdmedia.controller;

import com.jdmedia.service.AppContext;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public final class StatusBarController {
    private final AppContext context;
    @FXML private Label statusLabel, percentLabel;
    @FXML private ProgressBar statusProgress;
    @FXML private Button conversionButton;
    public StatusBarController(AppContext context) { this.context = context; }

    @FXML public void initialize() {
        statusLabel.textProperty().bind(context.status().messageProperty());
        statusProgress.progressProperty().bind(context.status().progressProperty());
        statusProgress.visibleProperty().bind(context.status().progressProperty().greaterThanOrEqualTo(0)); statusProgress.managedProperty().bind(statusProgress.visibleProperty());
        percentLabel.visibleProperty().bind(statusProgress.visibleProperty()); percentLabel.managedProperty().bind(percentLabel.visibleProperty());
        context.status().progressProperty().addListener((observable, oldValue, value) -> percentLabel.setText(value.doubleValue() < 0 ? "" : String.format("%.0f %%", value.doubleValue() * 100)));
        conversionButton.visibleProperty().bind(context.conversion().activeTaskProperty().isNotNull());
        conversionButton.managedProperty().bind(conversionButton.visibleProperty());
        conversionButton.textProperty().bind(Bindings.createStringBinding(() -> {
            double progress = context.status().progressProperty().get();
            return progress < 0 ? "● Convirtiendo…  Ver progreso >" : String.format("● Convirtiendo… %.0f %%  Ver progreso >", progress * 100);
        }, context.status().progressProperty(), context.conversion().activeTaskProperty()));
    }
    @FXML private void showConversion() { context.navigation().show("conversion-view.fxml"); }
}
