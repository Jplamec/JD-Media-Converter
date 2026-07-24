package com.jdmedia.model;

/** User-facing profiles; technical values remain internal. */
public enum ConversionPreset {
    HIGH_QUALITY("Alta calidad", "Más detalle y archivos más grandes", 19, 0.82),
    BALANCED("Equilibrada", "Buena relación entre tamaño y calidad", 23, 0.58),
    HIGH_COMPRESSION("Mucha compresión", "Ocupa mucho menos, con menor detalle", 27, 0.34),
    CLASSIC_LIBRARY("Película / Serie clásica", "HEVC muy comprimido, máximo 720p y pensado para ahorrar espacio", 34, 0.13);

    private final String label, description; private final int crf; private final double estimatedRemaining;
    ConversionPreset(String label, String description, int crf, double estimatedRemaining) { this.label = label; this.description = description; this.crf = crf; this.estimatedRemaining = estimatedRemaining; }
    public int crf() { return crf; }
    public double estimatedRemaining() { return estimatedRemaining; }
    public String description() { return description; }
    @Override public String toString() { return label; }
}
