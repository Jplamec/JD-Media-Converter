package com.jdmedia.service;

import com.google.gson.*;
import com.jdmedia.model.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public final class MediaAnalysisService {
    private static final Set<String> EXTENSIONS = Set.of("mkv", "mp4", "avi", "mov", "wmv", "flv", "ts", "vob", "webm", "m4v");
    private final SettingsService settings;
    public MediaAnalysisService(SettingsService settings) { this.settings = settings; }
    public List<Path> findVideos(Path folder) throws IOException {
        try (Stream<Path> paths = Files.walk(folder)) { return paths.filter(Files::isRegularFile).filter(this::isSupported).sorted().toList(); }
    }
    public MediaFile inspect(Path video) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(settings.get().ffprobePath, "-v", "error", "-show_entries", "format=duration,format_name,bit_rate", "-show_streams", "-of", "json", video.toString()).start();
        String output = new String(process.getInputStream().readAllBytes());
        String errors = new String(process.getErrorStream().readAllBytes());
        if (process.waitFor() != 0) throw new IOException(errors.isBlank() ? "FFprobe no pudo analizar el archivo" : errors.trim());
        JsonObject root = JsonParser.parseString(output).getAsJsonObject(); JsonObject format = root.getAsJsonObject("format");
        long duration = format.has("duration") ? Math.round(format.get("duration").getAsDouble()) : 0;
        long bitrate = format.has("bit_rate") ? format.get("bit_rate").getAsLong() : 0;
        List<StreamInfo> audio = new ArrayList<>(), subtitles = new ArrayList<>(); String codec = "—", resolution = "—";
        for (JsonElement element : root.getAsJsonArray("streams")) {
            JsonObject stream = element.getAsJsonObject(); String kind = value(stream, "codec_type");
            JsonObject tags = stream.has("tags") ? stream.getAsJsonObject("tags") : new JsonObject();
            StreamInfo info = new StreamInfo(stream.get("index").getAsInt(), value(stream,"codec_name"), value(tags,"language"), value(tags,"title"), stream.has("channels") ? stream.get("channels").getAsString() + " canales" : "");
            if ("video".equals(kind)) { codec = value(stream,"codec_name"); resolution = value(stream,"width") + "×" + value(stream,"height"); }
            else if ("audio".equals(kind)) audio.add(info); else if ("subtitle".equals(kind)) subtitles.add(info);
        }
        return new MediaFile(video, Files.size(video), duration, value(format,"format_name"), codec, resolution, bitrate, audio, subtitles);
    }
    private boolean isSupported(Path p) { String name=p.getFileName().toString(); int dot=name.lastIndexOf('.'); return dot>0 && EXTENSIONS.contains(name.substring(dot+1).toLowerCase()); }
    private String value(JsonObject o,String name) { return o.has(name) && !o.get(name).isJsonNull() ? o.get(name).getAsString() : "—"; }
}
