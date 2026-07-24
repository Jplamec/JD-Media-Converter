package com.jdmedia.controller;

import com.jdmedia.model.MediaType;
import com.jdmedia.service.AppContext;
import com.jdmedia.ui.components.MediaCard;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;

public final class HomeController {
    private final AppContext context;
    @FXML private HBox cardsContainer;

    public HomeController(AppContext context) { this.context = context; }

    @FXML public void initialize() {
        MediaCard movies = card("🎬", t("home.movies.title"), t("home.movies.description"), MediaType.MOVIES,
                t("home.feature.quality"), t("home.feature.source"), t("home.feature.tracks"));
        MediaCard series = card("📺", t("home.series.title"), t("home.series.description"), MediaType.SERIES,
                t("home.feature.quality"), t("home.feature.source"), t("home.feature.tracks"));
        cardsContainer.getChildren().setAll(movies, series);
    }

    private String t(String key) { return context.i18n().text(key); }
    private MediaCard card(String icon, String title, String text, MediaType type, String... features) {
        MediaCard card = new MediaCard(icon, title, text);
        for (String feature : features) card.addFeature(feature);
        card.setOnMouseClicked(event -> { FolderController.setType(type); context.navigation().show("folder-view.fxml"); });
        return card;
    }
}
