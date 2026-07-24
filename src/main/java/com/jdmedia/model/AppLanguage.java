package com.jdmedia.model;

import java.util.Locale;

public enum AppLanguage {
    SPANISH("ES", "🇪🇸 Español", new Locale("es")),
    ENGLISH("EN", "🇬🇧 English", Locale.ENGLISH),
    FRENCH("FR", "🇫🇷 Français", Locale.FRENCH),
    ITALIAN("IT", "🇮🇹 Italiano", Locale.ITALIAN),
    GERMAN("DE", "🇩🇪 Deutsch", Locale.GERMAN);

    private final String code;
    private final String label;
    private final Locale locale;
    AppLanguage(String code, String label, Locale locale) { this.code = code; this.label = label; this.locale = locale; }
    public String code() { return code; }
    public Locale locale() { return locale; }
    @Override public String toString() { return label; }
}
