package com.jdmedia.service;

import com.jdmedia.model.AppSettings;
import javafx.application.Platform;
import javafx.beans.property.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleConsumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Downloads, verifies and extracts FFmpeg into the user's application data folder.
 * The service uses multiple mirrors, throttled progress updates and safe ZIP extraction.
 */
public final class FfmpegInstallationService {
    private static final List<URI> MIRRORS = List.of(
            URI.create("https://github.com/BtbN/FFmpeg-Builds/releases/latest/download/ffmpeg-master-latest-win64-gpl.zip"),
            URI.create("https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip")
    );
    private static final Path INSTALL_DIR = Path.of(System.getProperty("user.home"), ".jd-media-converter", "tools", "ffmpeg");
    private static final int MAX_ATTEMPTS = 3;
    private static final int BUFFER_SIZE = 256 * 1024;
    private static final Duration PROGRESS_THROTTLE = Duration.ofMillis(250);

    private final SimpleBooleanProperty downloading = new SimpleBooleanProperty(false);
    private final SimpleDoubleProperty downloadProgress = new SimpleDoubleProperty(-1);
    private final SimpleStringProperty downloadMessage = new SimpleStringProperty("");

    private long downloadedBytes;
    private long totalBytes;
    private Instant downloadStart;
    private Instant lastProgressUpdate;

    public ReadOnlyBooleanProperty downloadingProperty() {
        return downloading;
    }

    public ReadOnlyDoubleProperty downloadProgressProperty() {
        return downloadProgress;
    }

    public ReadOnlyStringProperty downloadMessageProperty() {
        return downloadMessage;
    }

    public void beginDownload() {
        Platform.runLater(() -> {
            downloading.set(true);
            downloadProgress.set(0);
            downloadMessage.set("Descargando FFmpeg...");
        });
        downloadedBytes = 0;
        totalBytes = -1;
        downloadStart = Instant.now();
        lastProgressUpdate = Instant.EPOCH;
    }

    public void updateDownload(double value) {
        Platform.runLater(() -> {
            downloadProgress.set(value);
            downloadMessage.set(value < 0 ? "Descargando FFmpeg..." : String.format(Locale.ROOT, "Descargando FFmpeg: %.0f %%", value * 100));
        });
    }

    public void finishDownload(String message) {
        Platform.runLater(() -> {
            downloading.set(false);
            downloadProgress.set(1);
            downloadMessage.set(message);
        });
    }

    public void failDownload(String message) {
        Platform.runLater(() -> {
            downloading.set(false);
            downloadProgress.set(-1);
            downloadMessage.set(message);
        });
    }

    public boolean isAvailable(AppSettings settings) {
        if (settings == null) {
            return false;
        }
        return runs(settings.ffmpegPath) && runs(settings.ffprobePath);
    }

    public void install(AppSettings settings, DoubleConsumer progress) throws IOException {
        Objects.requireNonNull(settings, "settings cannot be null");
        DoubleConsumer progressConsumer = progress != null ? progress : value -> {
        };

        Path bin = INSTALL_DIR.resolve("bin");
        Path archive = INSTALL_DIR.resolve("ffmpeg-release-essentials.zip");
        Path partial = INSTALL_DIR.resolve("ffmpeg-release-essentials.zip.part");

        Files.createDirectories(bin);
        try {
            downloadArchive(archive, partial, progressConsumer);
            extractArchive(archive, bin);
            Path ffmpeg = bin.resolve("ffmpeg.exe");
            Path ffprobe = bin.resolve("ffprobe.exe");
            if (!Files.isRegularFile(ffmpeg) || !Files.isRegularFile(ffprobe)) {
                throw new IOException("El ZIP descargado no contenía ffmpeg.exe y ffprobe.exe.");
            }
            settings.ffmpegPath = ffmpeg.toAbsolutePath().toString();
            settings.ffprobePath = ffprobe.toAbsolutePath().toString();
            progressConsumer.accept(1d);
        } finally {
            Files.deleteIfExists(partial);
        }
    }

    private void downloadArchive(Path archive, Path partial, DoubleConsumer progress) throws IOException {
        IOException lastError = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            for (URI mirror : MIRRORS) {
                try {
                    Files.deleteIfExists(partial);
                    downloadFromMirror(mirror, archive, partial, progress);
                    return;
                } catch (IOException exception) {
                    if (lastError != null) {
                        exception.addSuppressed(lastError);
                    }
                    lastError = exception;
                }
            }
        }
        throw new IOException("La descarga falló tras " + MAX_ATTEMPTS + " intentos.", lastError);
    }

    private void downloadFromMirror(URI source, Path archive, Path partial, DoubleConsumer progress) throws IOException {
        System.out.println("[FFmpeg] Descargando desde: " + source);
        HttpURLConnection connection = openConnection(source);
        try {
            long total = readContentLength(connection, source);
            downloadedBytes = 0;
            totalBytes = total > 0 ? total : -1;
            downloadStart = Instant.now();
            lastProgressUpdate = Instant.EPOCH;
            System.out.println("[FFmpeg] Tamaño total: " + (totalBytes > 0 ? String.format("%.1f MB", bytesToMb(totalBytes)) : "desconocido"));

            try (InputStream raw = connection.getInputStream();
                 BufferedInputStream input = new BufferedInputStream(raw, BUFFER_SIZE);
                 OutputStream output = new BufferedOutputStream(Files.newOutputStream(partial, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING), BUFFER_SIZE)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                    downloadedBytes += read;
                    maybeUpdateProgress(progress);
                }
            }

            if (totalBytes > 0 && downloadedBytes != totalBytes) {
                throw new IOException(String.format("Descarga incompleta: %d de %d bytes.", downloadedBytes, totalBytes));
            }

            long totalElapsedMs = Duration.between(downloadStart, Instant.now()).toMillis();
            double avgSpeedMb = totalElapsedMs > 0 ? bytesToMb(downloadedBytes) / (totalElapsedMs / 1_000d) : 0;
            System.out.println(String.format("[FFmpeg] Descarga completada: %.1f MB en %.1f s (velocidad media: %.1f MB/s)",
                    bytesToMb(downloadedBytes), totalElapsedMs / 1_000d, avgSpeedMb));

            Files.move(partial, archive, StandardCopyOption.REPLACE_EXISTING);
            updateProgress(downloadedBytes, totalBytes, progress);
        } finally {
            connection.disconnect();
        }
    }

    private HttpURLConnection openConnection(URI source) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) source.toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent", "JD-Media-Converter/1.0");
        connection.setRequestProperty("Accept-Encoding", "identity");
        connection.setConnectTimeout(30_000);
        connection.setReadTimeout(600_000);
        
        long connectStart = System.currentTimeMillis();
        connection.connect();
        long connectTime = System.currentTimeMillis() - connectStart;
        System.out.println("[FFmpeg] Conexión establecida en " + connectTime + " ms");
        
        return connection;
    }

    private long readContentLength(HttpURLConnection connection, URI source) throws IOException {
        int response = connection.getResponseCode();
        if (response >= 400) {
            System.out.println("[FFmpeg] Error HTTP " + response + " al conectar con " + source);
            throw new IOException("El servidor respondió HTTP " + response + " al conectar con " + source);
        }
        String serverHeader = connection.getHeaderField("Server");
        if (serverHeader != null) {
            System.out.println("[FFmpeg] Servidor: " + serverHeader);
        }
        return connection.getContentLengthLong();
    }

    private void maybeUpdateProgress(DoubleConsumer progress) {
        Instant now = Instant.now();
        if (Duration.between(lastProgressUpdate, now).compareTo(PROGRESS_THROTTLE) < 0) {
            return;
        }
        lastProgressUpdate = now;
        updateProgress(downloadedBytes, totalBytes, progress);
    }

    private void updateProgress(long downloaded, long total, DoubleConsumer progress) {
        double fraction = total > 0 ? Math.min(1d, (double) downloaded / total) : -1d;
        progress.accept(fraction);
        Platform.runLater(() -> {
            if (fraction >= 0) {
                downloadProgress.set(fraction);
            }
            downloadMessage.set(buildDownloadMessage(downloaded, total, fraction));
        });
    }

    private String buildDownloadMessage(long downloaded, long total, double fraction) {
        String percentLine = fraction >= 0 ? String.format(Locale.ROOT, "%.0f%%", Math.min(100d, fraction * 100)) : "";
        String amountLine = total > 0
                ? String.format(Locale.ROOT, "%.1f MB / %.1f MB", bytesToMb(downloaded), bytesToMb(total))
                : String.format(Locale.ROOT, "%.1f MB", bytesToMb(downloaded));
        String speedLine = buildSpeedLine(downloaded);
        String etaLine = buildEtaLine(downloaded, total);

        StringBuilder message = new StringBuilder("Descargando FFmpeg...");
        if (!percentLine.isEmpty()) {
            message.append("\n").append(percentLine);
        }
        message.append("\n").append(amountLine);
        message.append("\n").append(speedLine);
        if (!etaLine.isBlank()) {
            message.append("\n").append(etaLine);
        }
        return message.toString();
    }

    private String buildSpeedLine(long downloaded) {
        Duration elapsed = Duration.between(downloadStart, Instant.now());
        double seconds = Math.max(0.001, elapsed.toMillis() / 1_000d);
        double speedMb = bytesToMb(downloaded) / seconds;
        return String.format(Locale.ROOT, "%.1f MB/s", speedMb);
    }

    private String buildEtaLine(long downloaded, long total) {
        if (total <= 0 || downloaded <= 0) {
            return "Tiempo restante: calculando...";
        }
        Duration elapsed = Duration.between(downloadStart, Instant.now());
        double seconds = Math.max(0.001, elapsed.toMillis() / 1_000d);
        double speedMb = bytesToMb(downloaded) / seconds;
        if (speedMb <= 0) {
            return "Tiempo restante: calculando...";
        }
        long remainingBytes = Math.max(0, total - downloaded);
        long remainingSeconds = Math.round(remainingBytes / (speedMb * 1_048_576d));
        return String.format(Locale.ROOT, "Tiempo restante: %d s", Math.max(0, remainingSeconds));
    }

    private static double bytesToMb(long bytes) {
        return bytes / 1_048_576d;
    }

    private void extractArchive(Path archive, Path bin) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(archive))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                String normalized = entry.getName().replace('\\', '/');
                if (entry.isDirectory()) {
                    continue;
                }
                if (normalized.endsWith("/bin/ffmpeg.exe") || normalized.endsWith("/bin/ffprobe.exe")) {
                    extractFile(zip, bin.resolve(Path.of(normalized).getFileName().toString()));
                } else if (normalized.equalsIgnoreCase("license") || normalized.endsWith("/license")) {
                    extractFile(zip, INSTALL_DIR.resolve("LICENSE"));
                }
            }
        } catch (IOException exception) {
            throw new IOException("El archivo descargado no es un ZIP válido: " + exception.getMessage(), exception);
        } finally {
            Files.deleteIfExists(archive);
        }
    }

    private void extractFile(ZipInputStream zip, Path target) throws IOException {
        Files.createDirectories(target.getParent());
        try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(target, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING), BUFFER_SIZE)) {
            zip.transferTo(output);
        }
    }

    private boolean runs(String executable) {
        if (executable == null || executable.isBlank()) {
            return false;
        }
        try {
            Process process = new ProcessBuilder(executable, "-version").redirectErrorStream(true).start();
            try (InputStream ignored = process.getInputStream()) {
                ignored.readNBytes(512);
            }
            return process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0;
        } catch (Exception exception) {
            return false;
        }
    }
}
