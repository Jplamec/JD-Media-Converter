package com.jdmedia.controller;

import com.jdmedia.model.AppSettings;
import com.jdmedia.App;
import com.jdmedia.service.AppContext;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public final class SettingsController {
    private final AppContext context;
    @FXML private TextField ffmpegField, ffprobeField, moviesField, seriesField, moviesOutputField, seriesOutputField; @FXML private ComboBox<com.jdmedia.model.AppLanguage> languageBox;
    @FXML private Label ffmpegStatus, downloadLabel; @FXML private ProgressBar downloadProgress;
    public SettingsController(AppContext context) { this.context = context; }
    @FXML public void initialize() { load(); checkFfmpeg(); downloadProgress.progressProperty().bind(context.ffmpegInstallation().downloadProgressProperty()); downloadProgress.visibleProperty().bind(context.ffmpegInstallation().downloadingProperty()); downloadProgress.managedProperty().bind(downloadProgress.visibleProperty()); downloadLabel.textProperty().bind(context.ffmpegInstallation().downloadMessageProperty()); downloadLabel.visibleProperty().bind(context.ffmpegInstallation().downloadingProperty()); downloadLabel.managedProperty().bind(downloadLabel.visibleProperty()); }
    private void load() { AppSettings s=context.settings().get(); ffmpegField.setText(s.ffmpegPath); ffprobeField.setText(s.ffprobePath); moviesField.setText(s.moviesFolder); seriesField.setText(s.seriesFolder); moviesOutputField.setText(s.moviesOutputFolder); seriesOutputField.setText(s.seriesOutputFolder); languageBox.getItems().setAll(com.jdmedia.model.AppLanguage.values()); languageBox.setValue(s.language); }
    @FXML private void save() { AppSettings s=context.settings().get(); com.jdmedia.model.AppLanguage previous=s.language; s.ffmpegPath=ffmpegField.getText().trim(); s.ffprobePath=ffprobeField.getText().trim(); s.moviesFolder=moviesField.getText().trim(); s.seriesFolder=seriesField.getText().trim(); s.moviesOutputFolder=moviesOutputField.getText().trim(); s.seriesOutputFolder=seriesOutputField.getText().trim(); s.language=languageBox.getValue(); context.settings().save(); if (s.language != previous) { App.reloadForLanguage(); return; } checkFfmpeg(); context.status().info("Configuración guardada."); }
    @FXML private void checkFfmpeg() { boolean ready=context.ffmpegInstallation().isAvailable(context.settings().get()); ffmpegStatus.setText(ready ? "✓ FFmpeg y FFprobe están listos para usar" : "FFmpeg no está disponible todavía"); ffmpegStatus.getStyleClass().setAll(ready ? "ffmpeg-ready" : "ffmpeg-missing"); }
    @FXML private void installFfmpeg() {
        Alert prompt=new Alert(Alert.AlertType.CONFIRMATION,"Se descargará FFmpeg Essentials para Windows en la carpeta de usuario. ¿Continuar?",ButtonType.YES,ButtonType.NO);
        prompt.setTitle("Descargar FFmpeg"); prompt.setHeaderText("Instalación de FFmpeg"); if(prompt.showAndWait().orElse(ButtonType.NO)!=ButtonType.YES) return;
        Task<Void> task=new Task<>() { @Override protected Void call() throws Exception { context.ffmpegInstallation().install(context.settings().get(), value -> updateProgress(value, 1)); context.settings().save(); return null; } };
        context.ffmpegInstallation().beginDownload();
        task.progressProperty().addListener((o,oldValue,newValue) -> { context.ffmpegInstallation().updateDownload(newValue.doubleValue()); context.status().progress(context.ffmpegInstallation().downloadMessageProperty().get(), newValue.doubleValue()); });
        task.setOnSucceeded(event -> { context.ffmpegInstallation().finishDownload("FFmpeg instalado en la carpeta de usuario"); load(); checkFfmpeg(); context.status().clearProgress(); context.status().info("FFmpeg instalado correctamente."); });
        task.setOnFailed(event -> { String error="Error al descargar FFmpeg: " + task.getException().getMessage(); context.ffmpegInstallation().failDownload(error); context.status().clearProgress(); context.status().error(error); });
        new Thread(task,"ffmpeg-installer").start();
    }
    @FXML private void home() { context.navigation().show("home-view.fxml"); }
}
