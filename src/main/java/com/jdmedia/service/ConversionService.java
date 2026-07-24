package com.jdmedia.service;

import com.jdmedia.model.*;
import javafx.concurrent.Task;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleConsumer;

public final class ConversionService {
    private final SettingsService settings; private final HistoryService history;
    private volatile ConversionSummary lastSummary; private Boolean nvencAvailable;
    private volatile Path lastOutputDirectory, lastOutputFile;
    private final ObjectProperty<Task<Void>> activeTask = new SimpleObjectProperty<>();
    private record ProcessResult(int code, String diagnostic) {}
    public ConversionService(SettingsService settings, HistoryService history) { this.settings = settings; this.history = history; }
    public ConversionSummary lastSummary() { return lastSummary; }
    public Path lastOutputDirectory() { return lastOutputDirectory; }
    public Path lastOutputFile() { return lastOutputFile; }
    public ObjectProperty<Task<Void>> activeTaskProperty() { return activeTask; }
    public Task<Void> activeTask() { return activeTask.get(); }
    public void setActiveTask(Task<Void> task) { activeTask.set(task); }
    public void clearActiveTask(Task<Void> task) { if (activeTask.get() == task) activeTask.set(null); }
    public synchronized boolean supportsNvenc() {
        if (nvencAvailable != null) return nvencAvailable;
        try { Process process = new ProcessBuilder(settings.get().ffmpegPath,"-hide_banner","-f","lavfi","-i","color=c=black:s=320x240:d=0.1","-c:v","h264_nvenc","-f","null","-").redirectErrorStream(true).start(); process.getInputStream().readAllBytes(); nvencAvailable = process.waitFor() == 0; }
        catch (Exception ignored) { nvencAvailable = false; }
        return nvencAvailable;
    }
    public Task<Void> createTask(List<MediaFile> media, ConversionOptions options, Path output) {
        return new Task<>() { @Override protected Void call() throws Exception {
            Files.createDirectories(output); lastOutputDirectory = output; lastOutputFile = null;
            Instant batch = Instant.now(); int done = 0, completed = 0; long original = 0, finalSize = 0; boolean nvenc = supportsNvenc();
            for (MediaFile file : media) {
                if (isCancelled()) break;
                Instant started = Instant.now(); Path target = output.resolve(stripExtension(file.fileName()) + "_JDPM.mp4");
                if (file.path().toAbsolutePath().normalize().equals(target.toAbsolutePath().normalize())) throw new IOException("La carpeta de salida no puede sobrescribir el archivo original: " + file.fileName());
                updateMessage("Convirtiendo" + (nvenc ? " con NVIDIA: " : ": ") + file.fileName());
                ProcessResult result = ConversionService.this.run(file, target, options, nvenc, done, media.size(), value -> updateProgress(value, 1), this::isCancelled); if (isCancelled()) break;
                long bytes = Files.exists(target) ? Files.size(target) : 0; boolean success = result.code() == 0 && bytes > 0;
                history.add(new ConversionRecord(started,file.path().toString(),target.toString(),file.sizeBytes(),bytes,Duration.between(started,Instant.now()).toSeconds(),(nvenc ? "NVENC" : "CPU") + " / calidad " + options.crf(),success ? "Completada" : "Error FFmpeg: " + result.diagnostic()));
                if (!success) throw new IOException((nvenc ? "NVENC" : "FFmpeg") + " no pudo convertir " + file.fileName() + ": " + result.diagnostic());
                lastOutputFile = target; completed++; original += file.sizeBytes(); finalSize += bytes;
                if (options.deleteOriginals()) Files.delete(file.path()); updateProgress(++done, media.size());
            }
            lastSummary = new ConversionSummary(media.size(),completed,original,finalSize,Duration.between(batch,Instant.now()).toSeconds(),isCancelled()); return null;
        }};
    }
    private ProcessResult run(MediaFile file, Path target, ConversionOptions options, boolean nvenc, int done, int total, DoubleConsumer reportProgress, BooleanSupplier cancelled) throws Exception {
        Process process = new ProcessBuilder(command(file,target,options,nvenc)).redirectErrorStream(true).start(); StringBuilder errors = new StringBuilder();
        try (BufferedReader reader = process.inputReader()) { String line; while ((line = reader.readLine()) != null && !cancelled.getAsBoolean()) {
            if (line.startsWith("out_time_ms=")) { try { long us = Long.parseLong(line.substring(12)); double p = file.durationSeconds() == 0 ? 0 : Math.min(1, us / (file.durationSeconds() * 1_000_000d)); reportProgress.accept((done + p) / total); } catch (NumberFormatException ignored) {} }
            else if (!line.startsWith("progress=")) { errors.append(line).append('\n'); if (errors.length() > 1800) errors.delete(0, errors.length() - 1800); }
        }}
        if (cancelled.getAsBoolean()) process.destroyForcibly(); int code = process.waitFor(); String diagnostic = errors.toString().trim(); return new ProcessResult(code, diagnostic.isBlank() ? "código " + code : diagnostic);
    }
    private List<String> command(MediaFile file, Path target, ConversionOptions options, boolean nvenc) {
        List<String> command = new ArrayList<>(List.of(settings.get().ffmpegPath,"-y","-progress","pipe:1","-nostats"));
        // Tone mapping needs normal frames, so we retain CUDA decoding but do not force CUDA frames for that optional path.
        if (nvenc) { command.addAll(List.of("-hwaccel","cuda")); if (!options.convertHdrToSdr()) command.addAll(List.of("-hwaccel_output_format","cuda")); }
        command.addAll(List.of("-i",file.path().toString(),"-map","0:v:0"));
        Integer audio = options.audioStreamIndexes().get(file.path()); if (audio != null) command.addAll(List.of("-map","0:" + audio));
        if (options.subtitles() == ConversionOptions.SubtitleMode.KEEP) { List<StreamInfo> compatible = file.subtitleStreams().stream().filter(this::isMp4TextSubtitle).toList(); for (StreamInfo subtitle : compatible) command.addAll(List.of("-map","0:" + subtitle.index())); if (!compatible.isEmpty()) command.addAll(List.of("-c:s","mov_text")); }
        boolean classic = "classic".equals(options.preset());
        if (options.convertHdrToSdr()) {
            command.addAll(List.of("-vf","zscale=t=linear:npl=100,format=gbrpf32le,tonemap=hable:desat=0,zscale=p=bt709:t=bt709:m=bt709:r=tv,format=yuv420p","-color_primaries","bt709","-color_trc","bt709","-colorspace","bt709"));
            if (nvenc && classic) command.addAll(List.of("-c:v","hevc_nvenc","-preset","p5","-rc","vbr","-cq",String.valueOf(options.crf()),"-b:v","0"));
            else if (nvenc) command.addAll(List.of("-c:v","h264_nvenc","-preset","p4","-rc","constqp","-qp",String.valueOf(options.crf())));
            else command.addAll(List.of("-c:v","libx264","-preset",options.preset(),"-crf",String.valueOf(options.crf())));
        } else if (nvenc && classic) command.addAll(List.of("-vf","scale_cuda=-2:min(720\\,ih):format=nv12","-c:v","hevc_nvenc","-preset","p5","-rc","vbr","-cq",String.valueOf(options.crf()),"-b:v","0"));
        else if (nvenc) command.addAll(List.of("-vf","scale_cuda=-2:1080:format=nv12","-c:v","h264_nvenc","-preset","p4","-rc","constqp","-qp",String.valueOf(options.crf())));
        else command.addAll(List.of("-c:v","libx264","-preset",options.preset(),"-crf",String.valueOf(options.crf()),"-vf","scale=-2:1080","-pix_fmt","yuv420p"));
        command.addAll(List.of("-c:a","aac","-b:a",classic ? "96k" : "192k","-ac","2","-movflags","+faststart",target.toString())); return command;
    }
    private boolean isMp4TextSubtitle(StreamInfo subtitle) { return Set.of("subrip","srt","ass","ssa","webvtt","mov_text").contains(subtitle.codec().toLowerCase()); }
    private String stripExtension(String name) { int dot = name.lastIndexOf('.'); return dot < 0 ? name : name.substring(0,dot); }
}
