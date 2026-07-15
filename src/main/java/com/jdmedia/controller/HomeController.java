package com.jdmedia.controller;
import com.jdmedia.model.MediaType;
import com.jdmedia.service.AppContext;
import com.jdmedia.ui.components.MediaCard;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
public final class HomeController {
    private final AppContext context; @FXML private HBox cardsContainer;
    public HomeController(AppContext context) {this.context=context;}
    @FXML public void initialize() {
        MediaCard movies=card("🎬","PELÍCULAS","Prepara películas para tu servidor multimedia.",MediaType.MOVIES,"Calidad configurable","Archivo o carpeta completa","Audio y subtítulos a elección");
        MediaCard series=card("📺","SERIES","Convierte episodios y temporadas a tu medida.",MediaType.SERIES,"Calidad configurable","Archivo o carpeta completa","Audio y subtítulos a elección");
        cardsContainer.getChildren().setAll(movies,series);
    }
    private MediaCard card(String icon,String title,String text,MediaType type,String... features) { MediaCard card=new MediaCard(icon,title,text); for(String feature:features)card.addFeature(feature); card.setOnMouseClicked(e->{FolderController.setType(type); context.navigation().show("folder-view.fxml");}); return card; }
}
