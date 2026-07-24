package com.jdmedia.service;

import com.jdmedia.model.AppLanguage;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/** Provides the active language bundle to FXML views and controllers. */
public final class LocalizationService {
    private final SettingsService settings;

    public LocalizationService(SettingsService settings) {
        this.settings = settings;
    }

    public Locale locale() {
        AppLanguage language = settings.get().language;
        return (language == null ? AppLanguage.SPANISH : language).locale();
    }

    public ResourceBundle bundle() {
        return ResourceBundle.getBundle("i18n.messages", locale());
    }

    public String text(String key, Object... values) {
        String value = bundle().getString(key);
        return values.length == 0 ? value : MessageFormat.format(value, values);
    }
}
