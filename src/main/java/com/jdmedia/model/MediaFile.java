package com.jdmedia.model;

import java.nio.file.Path;
import java.util.List;

public record MediaFile(Path path, long sizeBytes, long durationSeconds, String format, String videoCodec,
                        String resolution, long bitrate, List<StreamInfo> audioStreams, List<StreamInfo> subtitleStreams) {
    public String fileName() { return path.getFileName().toString(); }
}
