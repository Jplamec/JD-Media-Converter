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
    private static Stage primaryStage;
    private static AppContext context;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        context = new AppContext();
        primaryStage = stage;
        showMainScene();
        stage.centerOnScreen();
        stage.show();
    }

    private static void showMainScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/main-view.fxml"));
        loader.setControllerFactory(context::createController);
        loader.setResources(context.i18n().bundle());
        Scene scene = new Scene(loader.load(), 1200, 720);
        scene.getStylesheets().add(App.class.getResource("/css/main.css").toExternalForm());

        primaryStage.setTitle("JD Media Converter");
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(680);
        primaryStage.setScene(scene);
        if (primaryStage.getIcons().isEmpty()) primaryStage.getIcons().addAll(loadIcons());
        if (primaryStage.getIcons().isEmpty()) primaryStage.getIcons().add(AppIcon.create());
    }

    public static void reloadForLanguage() {
        try { showMainScene(); }
        catch (Exception exception) { throw new IllegalStateException("Could not reload the user interface", exception); }
    }

    private static List<Image> loadIcons() {
        List<Image> icons = new ArrayList<>();

        for (String resourcePath : List.of(
                "/images/app-icon-512.png",
                "/images/app-icon-256.png",
                "/images/app-icon.png",
                "/images/app-icon-128.png",
                "/images/app-icon.jpg",
                "/images/app-icon.jpeg")) {
            URL resource = App.class.getResource(resourcePath);
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
