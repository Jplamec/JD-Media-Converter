package com.jdmedia.service;

import java.awt.Taskbar;
import java.awt.Frame;
import java.awt.Window;

/** Mirrors the active conversion progress on the Windows taskbar icon when supported. */
public final class TaskbarProgressService {
    private final Taskbar taskbar;
    private final Window taskbarWindow;

    public TaskbarProgressService() {
        Taskbar available = null;
        Window window = null;
        try {
            if (Taskbar.isTaskbarSupported()) {
                Taskbar candidate = Taskbar.getTaskbar();
                if (candidate.isSupported(Taskbar.Feature.PROGRESS_VALUE)) {
                    available = candidate;
                    Frame frame = new Frame();
                    frame.setUndecorated(true);
                    frame.setType(Window.Type.UTILITY);
                    frame.setSize(1, 1);
                    frame.setLocation(-10_000, -10_000);
                    window = frame;
                }
            }
        } catch (UnsupportedOperationException | SecurityException ignored) {
            // The app continues normally on systems without taskbar progress support.
        }
        taskbar = available;
        taskbarWindow = window;
    }

    public void update(double progress) {
        if (taskbar == null) return;
        try {
            taskbar.setWindowProgressState(taskbarWindow, Taskbar.State.NORMAL);
            taskbar.setWindowProgressValue(taskbarWindow, Math.max(0, Math.min(100, (int) Math.round(progress * 100))));
        } catch (UnsupportedOperationException ignored) { }
    }

    public void clear() {
        if (taskbar == null) return;
        try { taskbar.setWindowProgressState(taskbarWindow, Taskbar.State.OFF); }
        catch (UnsupportedOperationException ignored) { }
    }

    public void failed() {
        if (taskbar == null) return;
        try { taskbar.setWindowProgressState(taskbarWindow, Taskbar.State.ERROR); }
        catch (UnsupportedOperationException ignored) { }
    }
}
