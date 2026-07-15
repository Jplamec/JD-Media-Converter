package com.jdmedia.service;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.io.IOException;

/** Loads central views. Controllers receive dependencies through this small application service. */
public final class NavigationService {
    private final StackPane contentPane;
    private final AppContext context;

    public NavigationService(StackPane contentPane, AppContext context) {
        this.contentPane = contentPane;
        this.context = context;
    }

    public void show(String view) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + view));
            loader.setControllerFactory(type -> context.createController(type));
            Node node = loader.load();
            contentPane.getChildren().setAll(node);
        } catch (IOException | RuntimeException exception) {
            context.status().error("No se pudo abrir la pantalla: " + view + ". " + rootMessage(exception));
        }
    }

    private String rootMessage(Exception exception) {
        Throwable cause = exception;
        while (cause.getCause() != null) cause = cause.getCause();
        return cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
    }
}
