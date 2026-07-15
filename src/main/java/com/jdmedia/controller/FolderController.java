package com.jdmedia.controller;
import com.jdmedia.model.MediaType;
import com.jdmedia.service.AppContext;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import java.nio.file.*;
import java.util.List;
public final class FolderController {
 private static MediaType type; private final AppContext context; @FXML private TextField folderField; @FXML private Label titleLabel;
 public FolderController(AppContext context){this.context=context;} public static void setType(MediaType value){type=value;} public static MediaType type(){return type;}
 @FXML public void initialize(){titleLabel.setText(type==MediaType.MOVIES?"Selecciona películas":"Selecciona series");folderField.setText(type==MediaType.MOVIES?context.settings().get().moviesFolder:context.settings().get().seriesFolder);}
 @FXML private void chooseFolder(){DirectoryChooser c=new DirectoryChooser();c.setTitle("Selecciona una carpeta");java.io.File f=c.showDialog(folderField.getScene().getWindow());if(f!=null)folderField.setText(f.getPath());}
 @FXML private void chooseFile(){FileChooser c=new FileChooser();c.setTitle("Selecciona un vídeo");c.getExtensionFilters().add(new FileChooser.ExtensionFilter("Vídeos","*.mkv","*.mp4","*.avi","*.mov","*.wmv","*.flv","*.ts","*.vob","*.webm","*.m4v"));java.io.File f=c.showOpenDialog(folderField.getScene().getWindow());if(f!=null)folderField.setText(f.getPath());}
 @FXML private void continueFlow(){try{Path path=Path.of(folderField.getText());if(!Files.exists(path))throw new IllegalArgumentException("Selecciona una carpeta o un archivo válido.");if(Files.isDirectory(path)){if(type==MediaType.MOVIES)context.settings().get().moviesFolder=path.toString();else context.settings().get().seriesFolder=path.toString();AnalysisController.setFiles(null);AnalysisController.setFolder(path);}else{AnalysisController.setFiles(List.of(path));AnalysisController.setFolder(path.getParent());}context.settings().save();context.navigation().show("analysis-view.fxml");}catch(Exception e){context.status().error(e.getMessage());}}
 @FXML private void back(){context.navigation().show("home-view.fxml");}
}
