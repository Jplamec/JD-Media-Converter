package com.jdmedia.controller;

import com.jdmedia.model.MediaType;
import com.jdmedia.service.AppContext;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class FolderController {
    private static MediaType type;
    private final AppContext context;
    @FXML private TextField folderField;
    @FXML private Label titleLabel;

    public FolderController(AppContext context) { this.context = context; }
    public static void setType(MediaType value) { type = value; }
    public static MediaType type() { return type; }

    @FXML public void initialize() {
        titleLabel.setText(t(type == MediaType.MOVIES ? "folder.moviesTitle" : "folder.seriesTitle"));
        folderField.setText(type == MediaType.MOVIES ? context.settings().get().moviesFolder : context.settings().get().seriesFolder);
    }
    @FXML private void chooseFolder() {
        DirectoryChooser chooser = new DirectoryChooser(); chooser.setTitle(t("folder.chooseFolder"));
        java.io.File file = chooser.showDialog(folderField.getScene().getWindow());
        if (file != null) folderField.setText(file.getPath());
    }
    @FXML private void chooseFile() {
        FileChooser chooser = new FileChooser(); chooser.setTitle(t("folder.chooseFile"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(t("folder.videos"), "*.mkv", "*.mp4", "*.avi", "*.mov", "*.wmv", "*.flv", "*.ts", "*.vob", "*.webm", "*.m4v"));
        java.io.File file = chooser.showOpenDialog(folderField.getScene().getWindow());
        if (file != null) folderField.setText(file.getPath());
    }
    @FXML private void continueFlow() {
        try {
            Path path = Path.of(folderField.getText());
            if (!Files.exists(path)) throw new IllegalArgumentException(t("folder.invalid"));
            if (Files.isDirectory(path)) {
                if (type == MediaType.MOVIES) context.settings().get().moviesFolder = path.toString(); else context.settings().get().seriesFolder = path.toString();
                AnalysisController.setFiles(null); AnalysisController.setFolder(path);
            } else { AnalysisController.setFiles(List.of(path)); AnalysisController.setFolder(path.getParent()); }
            context.settings().save(); context.navigation().show("analysis-view.fxml");
        } catch (Exception exception) { context.status().error(exception.getMessage()); }
    }
    @FXML private void back() { context.navigation().show("home-view.fxml"); }
    private String t(String key) { return context.i18n().text(key); }
}
