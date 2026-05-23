package com.orderingsystem.fx.presentation.ux;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

import java.util.function.Consumer;

/**
 * Phản hồi trạng thái toàn cục trên JavaFX Application Thread.
 */
public final class UiFeedback {

    private static Label statusLabel;
    private static ProgressIndicator busyIndicator;
    private static int busyCount;

    private UiFeedback() {
    }

    public static void bind(Label status, ProgressIndicator busy) {
        statusLabel = status;
        busyIndicator = busy;
        setBusy(false);
        setStatus("Sẵn sàng.");
    }

    public static void setStatus(String message) {
        runOnFxThread(() -> {
            if (statusLabel != null) {
                statusLabel.setText(message);
            }
        });
    }

    public static void setBusy(boolean busy) {
        runOnFxThread(() -> updateBusy(busy));
    }

    public static void runWithFeedback(String busyMessage, Runnable action, String successMessage) {
        setBusy(true);
        setStatus(busyMessage);
        try {
            action.run();
            setStatus(successMessage);
        } finally {
            setBusy(false);
        }
    }

    public static void runWithFeedback(String busyMessage, Consumer<Runnable> actionWithSuccess) {
        setBusy(true);
        setStatus(busyMessage);
        try {
            actionWithSuccess.accept(() -> setStatus("Hoàn tất."));
        } finally {
            setBusy(false);
        }
    }

    private static void updateBusy(boolean busy) {
        if (busy) {
            busyCount++;
        } else {
            busyCount = Math.max(0, busyCount - 1);
        }
        boolean show = busyCount > 0;
        if (busyIndicator != null) {
            busyIndicator.setVisible(show);
            busyIndicator.setManaged(show);
        }
    }

    private static void runOnFxThread(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }

    public static void clear() {
        statusLabel = null;
        busyIndicator = null;
        busyCount = 0;
    }
}
