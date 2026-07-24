package com.jdmedia.controller;

import com.jdmedia.model.ConversionOptions;
import com.jdmedia.model.MediaType;
import com.jdmedia.service.AppContext;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.nio.file.Path;

public final class ConversionController {
    private final AppContext context;
    private Task<Void> task;
    private Path output;
    @FXML private Label currentFileLabel, percentLabel, encoderLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Button startButton, cancelButton;
    @FXML private TextField destinationField;
    @FXML private CheckBox deleteOriginals;

    public ConversionController(AppContext context) { this.context = context; }

    @FXML public void initialize() {
        String saved = FolderController.type() == MediaType.MOVIES ? context.settings().get().moviesOutputFolder : context.settings().get().seriesOutputFolder;
        destinationField.setText(saved); percentLabel.setText("");
        encoderLabel.setText(context.conversion().supportsNvenc() ? "Aceleración NVIDIA NVENC disponible" : "NVENC no disponible: se usará el codificador por CPU.");
        Task<Void> active = context.conversion().activeTask();
        if (active != null && !active.isDone()) resume(active);
        else currentFileLabel.setText("Selecciona una carpeta de destino para comenzar.");
    }

    private void resume(Task<Void> active) {
        task = active;
        destinationField.setDisable(true); deleteOriginals.setDisable(true); startButton.setDisable(true);
        attachProgress(active);
    }

    @FXML private void chooseDestination() {
        DirectoryChooser chooser = new DirectoryChooser(); chooser.setTitle("Selecciona la carpeta de destino");
        if (!destinationField.getText().isBlank()) { File file = new File(destinationField.getText()); if (file.isDirectory()) chooser.setInitialDirectory(file); }
        File selected = chooser.showDialog(destinationField.getScene().getWindow());
        if (selected != null) { output = selected.toPath(); destinationField.setText(output.toString()); }
    }

    @FXML private void start() {
        try {
            if (destinationField.getText().isBlank()) throw new IllegalArgumentException("Selecciona una carpeta de destino.");
            output = output == null ? Path.of(destinationField.getText()) : output;
            if (FolderController.type() == MediaType.MOVIES) context.settings().get().moviesOutputFolder = output.toString(); else context.settings().get().seriesOutputFolder = output.toString();
            context.settings().save();
            ConversionOptions base = SummaryController.options();
            ConversionOptions options = new ConversionOptions(base.preset(), base.crf(), base.audioStreamIndexes(), base.subtitles(), deleteOriginals.isSelected(), base.convertHdrToSdr());
            task = context.conversion().createTask(AnalysisController.media(), options, output);
            context.conversion().setActiveTask(task);
            destinationField.setDisable(true); deleteOriginals.setDisable(true); startButton.setDisable(true);
            attachProgress(task);
            task.setOnSucceeded(event -> {
                context.taskbarProgress().clear(); context.status().clearProgress(); context.conversion().clearActiveTask(task);
                if (task.isCancelled()) { context.status().info("Conversión cancelada."); context.navigation().show("home-view.fxml"); }
                else { context.status().info("Conversión finalizada."); context.navigation().show("result-view.fxml"); }
            });
            task.setOnFailed(event -> {
                context.taskbarProgress().failed(); context.status().clearProgress(); context.conversion().clearActiveTask(task);
                String detail = task.getException().getMessage(); context.status().error(detail);
                Alert alert = new Alert(Alert.AlertType.ERROR); alert.setTitle("Error de conversión"); alert.setHeaderText("NVENC no pudo convertir el archivo"); alert.setContentText(detail); alert.getDialogPane().setPrefWidth(850); alert.showAndWait();
            });
            new Thread(task, "media-conversion").start();
        } catch (Exception exception) { context.status().error(exception.getMessage()); }
    }

    private void attachProgress(Task<Void> active) {
        currentFileLabel.textProperty().bind(active.messageProperty()); progressBar.progressProperty().bind(active.progressProperty());
        active.progressProperty().addListener((observable, oldValue, value) -> {
            double progress = value.doubleValue(); context.status().setProgress(progress); if (progress >= 0) context.taskbarProgress().update(progress);
            percentLabel.setText(progress < 0 ? "Preparando…" : String.format("%.0f %%", progress * 100));
        });
    }

    @FXML private void cancel() {
        if (task != null) task.cancel(true);
        context.taskbarProgress().clear(); context.status().clearProgress(); context.status().info("Cancelando conversión…");
    }
}
