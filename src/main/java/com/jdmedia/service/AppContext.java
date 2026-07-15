package com.jdmedia.service;

import com.jdmedia.controller.*;

/** Composition root: application-wide services are created once, outside controllers. */
public final class AppContext {
    private final StatusService status = new StatusService();
    private final SettingsService settings = new SettingsService();
    private final FfmpegInstallationService ffmpegInstallation = new FfmpegInstallationService();
    private final MediaAnalysisService analysis = new MediaAnalysisService(settings);
    private final HistoryService history = new HistoryService();
    private final ConversionService conversion = new ConversionService(settings, history);
    private NavigationService navigation;

    public void setNavigation(NavigationService navigation) { this.navigation = navigation; }
    public NavigationService navigation() { return navigation; }
    public StatusService status() { return status; }
    public SettingsService settings() { return settings; }
    public FfmpegInstallationService ffmpegInstallation() { return ffmpegInstallation; }
    public MediaAnalysisService analysis() { return analysis; }
    public HistoryService history() { return history; }
    public ConversionService conversion() { return conversion; }

    public Object createController(Class<?> type) {
        if (type == MainController.class) return new MainController(this);
        if (type == HomeController.class) return new HomeController(this);
        if (type == FolderController.class) return new FolderController(this);
        if (type == AnalysisController.class) return new AnalysisController(this);
        if (type == SummaryController.class) return new SummaryController(this);
        if (type == ConversionController.class) return new ConversionController(this);
        if (type == ResultController.class) return new ResultController(this);
        if (type == HistoryController.class) return new HistoryController(this);
        if (type == SettingsController.class) return new SettingsController(this);
        if (type == HeaderController.class) return new HeaderController(this);
        if (type == StatusBarController.class) return new StatusBarController(this);
        try { return type.getDeclaredConstructor().newInstance(); }
        catch (ReflectiveOperationException exception) { throw new IllegalStateException(exception); }
    }
}
