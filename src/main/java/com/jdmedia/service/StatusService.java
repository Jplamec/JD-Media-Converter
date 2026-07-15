package com.jdmedia.service;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public final class StatusService {
    private final SimpleStringProperty message = new SimpleStringProperty("Listo para empezar");
    private final SimpleDoubleProperty progress = new SimpleDoubleProperty(-1);
    public ReadOnlyStringProperty messageProperty() { return message; }
    public ReadOnlyDoubleProperty progressProperty() { return progress; }
    public void info(String value) { message.set(value); }
    public void error(String value) { message.set("Error: " + value); progress.set(-1); }
    public void progress(String value, double valueProgress) { message.set(value); progress.set(valueProgress); }
    public void setProgress(double value) { progress.set(value); }
    public void clearProgress() { progress.set(-1); }
}
