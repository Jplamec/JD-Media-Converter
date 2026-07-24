package com.jdmedia.controller;

import com.jdmedia.App;
import com.jdmedia.model.AppLanguage;
import com.jdmedia.service.AppContext;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.awt.Desktop;
import java.net.URI;

public final class HeaderController {
    private final AppContext context;
    @FXML private ComboBox<AppLanguage> languageBox;
    public HeaderController(AppContext context) { this.context = context; }

    @FXML public void initialize() {
        languageBox.getItems().setAll(AppLanguage.values());
        languageBox.setConverter(new StringConverter<>() {
            @Override public String toString(AppLanguage language) { return language == null ? "" : language.code(); }
            @Override public AppLanguage fromString(String value) { return null; }
        });
        languageBox.setValue(context.settings().get().language);
        languageBox.valueProperty().addListener((observable, previous, selected) -> {
            if (selected != null && selected != previous) {
                context.settings().get().language = selected;
                context.settings().save();
                App.reloadForLanguage();
            }
        });
    }
    @FXML private void openHistory() { context.navigation().show("history-view.fxml"); }
    @FXML private void openSettings() { context.navigation().show("settings-view.fxml"); }
    @FXML private void openHelp() {
        Dialog<Void> dialog = new Dialog<>(); dialog.setTitle("Ayuda — JD Media Converter"); dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        VBox content = new VBox(10); content.setPadding(new Insets(10));
        content.getChildren().add(new Label("Convierte películas y series para tu biblioteca multimedia.\n\n1. Elige Películas o Series.\n2. Selecciona una carpeta o un archivo.\n3. Elige audio, subtítulos y calidad.\n4. Selecciona el destino e inicia la conversión.\n\nUso de FFmpeg: este programa usa FFmpeg para la conversión multimedia, con licencia y documentación en el proyecto oficial."));
        Hyperlink github = link("GitHub — Jplamec", "https://github.com/Jplamec");
        Hyperlink linkedin = link("LinkedIn — José David Plaza Meca", "https://www.linkedin.com/in/jos%C3%A9-david-plaza-meca-67203a275/?skipRedirect=true");
        Hyperlink ffmpeg = link("FFmpeg — licencia y proyecto", "https://www.ffmpeg.org/legal.html");
        content.getChildren().addAll(github, linkedin, ffmpeg); dialog.getDialogPane().setContent(content); dialog.getDialogPane().setPrefWidth(540); dialog.showAndWait();
    }
    private Hyperlink link(String text, String url) { Hyperlink link = new Hyperlink(text); link.setOnAction(event -> { try { Desktop.getDesktop().browse(URI.create(url)); } catch (Exception exception) { context.status().error("No se pudo abrir el enlace."); } }); return link; }
}
