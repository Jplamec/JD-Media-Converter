package com.jdmedia.util;

import java.nio.file.Files;
import java.nio.file.Path;

/** Resolves binaries shipped beside the application, falling back to the system PATH. */
public final class BundledToolLocator {
    private BundledToolLocator() { }
    public static String ffmpeg() { return locate("ffmpeg"); }
    public static String ffprobe() { return locate("ffprobe"); }
    private static String locate(String name) {
        String executable = System.getProperty("os.name").toLowerCase().contains("win") ? name + ".exe" : name;
        Path bundled = Path.of("tools", "ffmpeg", "bin", executable).toAbsolutePath();
        return Files.isRegularFile(bundled) ? bundled.toString() : name;
    }
}
