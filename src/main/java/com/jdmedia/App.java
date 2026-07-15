package com.jdmedia;

import com.jdmedia.service.AppContext;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import com.jdmedia.ui.AppIcon;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class App extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        AppContext context = new AppContext();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-view.fxml"));
        loader.setControllerFactory(context::createController);
        Scene scene = new Scene(loader.load(), 1200, 720);
        scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());

        stage.setTitle("JD Media Converter");
        stage.setMinWidth(1100);
        stage.setMinHeight(680);
        stage.setScene(scene);
        stage.getIcons().addAll(loadIcons());
        if (stage.getIcons().isEmpty()) stage.getIcons().add(AppIcon.create());
        stage.centerOnScreen();
        stage.show();
    }

    private List<Image> loadIcons() {
        List<Image> icons = new ArrayList<>();

        for (String resourcePath : List.of(
                "/images/app-icon-512.png",
                "/images/app-icon-256.png",
                "/images/app-icon.png",
                "/images/app-icon-128.png",
                "/images/app-icon.jpg",
                "/images/app-icon.jpeg")) {
            URL resource = getClass().getResource(resourcePath);
            if (resource != null) {
                try {
                    icons.add(new Image(resource.toExternalForm()));
                } catch (Exception ignored) {
                    // Ignore invalid image resources.
                }
            }
        }

        String overridePath = System.getProperty("app.icon");
        if (overridePath != null && !overridePath.isBlank()) {
            try {
                icons.add(0, new Image(new File(overridePath).toURI().toString()));
            } catch (Exception ignored) {
                // Ignore invalid override image.
            }
        }

        return icons;
    }

}
