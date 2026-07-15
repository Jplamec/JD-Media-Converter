package com.jdmedia.service;

import com.jdmedia.model.AppSettings;
import javafx.beans.property.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleConsumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** Downloads, verifies and extracts a user-approved FFmpeg build into tools/ffmpeg. */
public final class FfmpegInstallationService {
    private static final URI WINDOWS_ESSENTIALS = URI.create("https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip");
    private static final Path INSTALL_DIR = Path.of("tools", "ffmpeg");
    private final SimpleBooleanProperty downloading = new SimpleBooleanProperty(false);
    private final SimpleDoubleProperty downloadProgress = new SimpleDoubleProperty(-1);
    private final SimpleStringProperty downloadMessage = new SimpleStringProperty("");

    public ReadOnlyBooleanProperty downloadingProperty() { return downloading; }
    public ReadOnlyDoubleProperty downloadProgressProperty() { return downloadProgress; }
    public ReadOnlyStringProperty downloadMessageProperty() { return downloadMessage; }
    public void beginDownload() { downloading.set(true); downloadProgress.set(0); downloadMessage.set("Descargando FFmpeg: 0 %"); }
    public void updateDownload(double value) { downloadProgress.set(value); downloadMessage.set(value < 0 ? "Descargando FFmpeg…" : String.format("Descargando FFmpeg: %.0f %%", value * 100)); }
    public void finishDownload(String message) { downloading.set(false); downloadProgress.set(1); downloadMessage.set(message); }
    public void failDownload(String message) { downloading.set(false); downloadProgress.set(-1); downloadMessage.set(message); }
    public boolean isAvailable(AppSettings settings) { return runs(settings.ffmpegPath) && runs(settings.ffprobePath); }

    public void install(AppSettings settings, DoubleConsumer progress) throws IOException {
        Path bin = INSTALL_DIR.resolve("bin"), archive = INSTALL_DIR.resolve("ffmpeg-release-essentials.zip");
        Files.createDirectories(bin); downloadArchive(archive, progress); extractArchive(archive, bin);
        Path ffmpeg = bin.resolve("ffmpeg.exe"), ffprobe = bin.resolve("ffprobe.exe");
        if (!Files.isRegularFile(ffmpeg) || !Files.isRegularFile(ffprobe)) throw new IOException("El ZIP descargado no contenía ffmpeg.exe y ffprobe.exe.");
        settings.ffmpegPath = ffmpeg.toAbsolutePath().toString(); settings.ffprobePath = ffprobe.toAbsolutePath().toString(); progress.accept(1d);
    }

    private void downloadArchive(Path archive, DoubleConsumer progress) throws IOException {
        Path partial = archive.resolveSibling(archive.getFileName() + ".part"); IOException lastError = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            Files.deleteIfExists(partial); HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) WINDOWS_ESSENTIALS.toURL().openConnection();
                connection.setRequestProperty("User-Agent", "JD-Media-Converter/1.0"); connection.setRequestProperty("Accept-Encoding", "identity");
                connection.setConnectTimeout(30_000); connection.setReadTimeout(600_000);
                int response = connection.getResponseCode(); if (response >= 400) throw new IOException("El servidor respondió HTTP " + response);
                long total = connection.getContentLengthLong(); long downloaded = 0;
                try (InputStream input = new BufferedInputStream(connection.getInputStream()); OutputStream output = new BufferedOutputStream(Files.newOutputStream(partial))) {
                    byte[] buffer = new byte[64 * 1024]; int read;
                    while ((read = input.read(buffer)) >= 0) { output.write(buffer, 0, read); downloaded += read; progress.accept(total > 0 ? Math.min(1d, (double) downloaded / total) : -1d); }
                }
                if (total > 0 && downloaded != total) throw new IOException("Descarga incompleta: " + downloaded + " de " + total + " bytes.");
                Files.move(partial, archive, StandardCopyOption.REPLACE_EXISTING); return;
            } catch (IOException exception) { lastError = exception; }
            finally { if (connection != null) connection.disconnect(); Files.deleteIfExists(partial); }
        }
        throw new IOException("La descarga falló tras 3 intentos. " + (lastError == null ? "" : lastError.getMessage()), lastError);
    }

    private void extractArchive(Path archive, Path bin) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(archive))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                String name = entry.getName().replace('\\', '/');
                if (entry.isDirectory() || !(name.endsWith("/bin/ffmpeg.exe") || name.endsWith("/bin/ffprobe.exe") || name.matches(".*/(LICENSE|COPYING.*)"))) continue;
                Path target = name.contains("/bin/") ? bin.resolve(Path.of(name).getFileName().toString()) : INSTALL_DIR.resolve(Path.of(name).getFileName().toString());
                Files.copy(zip, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) { throw new IOException("El archivo descargado no es un ZIP válido: " + exception.getMessage(), exception); }
        finally { Files.deleteIfExists(archive); }
    }

    private boolean runs(String executable) { try { Process process = new ProcessBuilder(executable, "-version").redirectErrorStream(true).start(); process.getInputStream().readNBytes(512); return process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0; } catch (Exception exception) { return false; } }
}
