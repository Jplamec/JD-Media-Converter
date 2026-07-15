package com.jdmedia.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jdmedia.model.AppSettings;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SettingsService {
    private static final Path FILE = Path.of(System.getProperty("user.home"), ".jd-media-converter", "settings.json");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private AppSettings settings = load();
    public AppSettings get() { return settings; }
    public void save() {
        try { Files.createDirectories(FILE.getParent()); Files.writeString(FILE, gson.toJson(settings)); }
        catch (IOException exception) { throw new IllegalStateException("No se pudieron guardar las preferencias", exception); }
    }
    private AppSettings load() {
        try { return Files.exists(FILE) ? gson.fromJson(Files.readString(FILE), AppSettings.class) : new AppSettings(); }
        catch (Exception exception) { return new AppSettings(); }
    }
}
