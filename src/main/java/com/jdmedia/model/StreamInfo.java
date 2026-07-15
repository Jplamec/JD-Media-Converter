package com.jdmedia.model;
public record StreamInfo(int index, String codec, String language, String title, String channels) {
    public String label() {
        String name = title == null || title.isBlank() ? codec : title;
        return (language == null || language.isBlank() ? "Sin idioma" : language) + " — " + name + (channels == null || channels.isBlank() ? "" : " (" + channels + ")");
    }
}
