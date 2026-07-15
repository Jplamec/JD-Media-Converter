package com.jdmedia.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public final class MediaCard extends VBox {
    private final VBox features = new VBox(8);
    public MediaCard(String icon, String title, String description) {
        getStyleClass().add("media-card"); setAlignment(Pos.TOP_CENTER); setPadding(new Insets(18)); setSpacing(9); setPrefSize(235,275); setMaxSize(300,350);
        Label iconLabel=new Label(icon); iconLabel.getStyleClass().add("media-card-icon");
        Label titleLabel=new Label(title); titleLabel.getStyleClass().add("media-card-title");
        Label descriptionLabel=new Label(description); descriptionLabel.getStyleClass().add("media-card-description"); descriptionLabel.setWrapText(true); descriptionLabel.setMaxWidth(195);
        Region spacer=new Region(); VBox.setVgrow(spacer, Priority.ALWAYS); features.setAlignment(Pos.CENTER_LEFT);
        getChildren().addAll(iconLabel,titleLabel,descriptionLabel,spacer,features);
    }
    public void addFeature(String feature) { Label label=new Label("✓  "+feature); label.getStyleClass().add("media-card-feature"); features.getChildren().add(label); }
}
