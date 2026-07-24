package com.jdmedia.controller;

import com.jdmedia.service.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.awt.Desktop;
import java.net.URI;
import javafx.scene.layout.StackPane;

public final class MainController {
    private final AppContext context; @FXML private StackPane contentPane;
    public MainController(AppContext context) { this.context=context; }
    @FXML public void initialize() { NavigationService navigation=new NavigationService(contentPane,context); context.setNavigation(navigation); navigation.show("home-view.fxml"); Platform.runLater(() -> { ensureFfmpeg(); checkUpdates(); }); }
    private void ensureFfmpeg() {
        if (context.ffmpegInstallation().isAvailable(context.settings().get())) return;
        Alert prompt = new Alert(Alert.AlertType.CONFIRMATION, "JD Media Converter necesita FFmpeg y FFprobe para analizar y convertir vídeo. ¿Quieres descargar la compilación Windows Essentials ahora? Se guardará fuera del repositorio, en tu carpeta de usuario.", ButtonType.YES, ButtonType.NO);
        prompt.setTitle("Instalar FFmpeg"); prompt.setHeaderText("FFmpeg no está disponible");
        if (prompt.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) { context.status().error("FFmpeg no está instalado. Puedes configurarlo desde Configuración."); return; }
        Task<Void> task = new Task<>() { @Override protected Void call() throws Exception { updateMessage("Descargando FFmpeg…"); context.ffmpegInstallation().install(context.settings().get(), value -> updateProgress(value, 1)); context.settings().save(); return null; } };
        context.ffmpegInstallation().beginDownload();
        task.progressProperty().addListener((observable, oldValue, value) -> { context.ffmpegInstallation().updateDownload(value.doubleValue()); context.status().progress(context.ffmpegInstallation().downloadMessageProperty().get(), value.doubleValue()); });
        task.setOnSucceeded(event -> { context.ffmpegInstallation().finishDownload("FFmpeg instalado en la carpeta de usuario"); context.status().clearProgress(); context.status().info("FFmpeg se ha instalado correctamente."); });
        task.setOnFailed(event -> { String error="Error al descargar FFmpeg: " + task.getException().getMessage(); context.ffmpegInstallation().failDownload(error); context.status().clearProgress(); context.status().error(error); });
        context.status().progress("Descargando FFmpeg: 0 %", 0); new Thread(task, "ffmpeg-installer").start();
    }
    private void checkUpdates() {
        Task<java.util.Optional<UpdateService.Release>> task=new Task<>() { @Override protected java.util.Optional<UpdateService.Release> call(){return context.updates().latestRelease();} };
        task.setOnSucceeded(event -> task.getValue().ifPresent(this::showUpdate)); new Thread(task,"update-check").start();
    }
    private void showUpdate(UpdateService.Release release) {
        Alert alert=new Alert(Alert.AlertType.INFORMATION,"Hay una nueva versión disponible.\n\nVersión actual: " + context.updates().currentVersion() + "\nNueva versión: " + release.version(),ButtonType.YES,ButtonType.NO);
        alert.setTitle("Actualización disponible"); alert.setHeaderText("JD Media Converter " + release.version());
        ButtonType download=new ButtonType("Descargar"); ButtonType later=new ButtonType("Más tarde"); alert.getButtonTypes().setAll(download,later);
        if(alert.showAndWait().orElse(later)==download)try{Desktop.getDesktop().browse(URI.create(release.url()));}catch(Exception ignored){}
    }
}
