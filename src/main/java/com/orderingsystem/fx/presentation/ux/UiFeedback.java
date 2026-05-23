package com.orderingsystem.fx.presentation.ux;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Heuristic #1 — Phản hồi trạng thái toàn cục (thanh trạng thái + busy).
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
        runOnFx(() -> {
            if (statusLabel != null) {
                statusLabel.setText(message);
            }
        });
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

    private static void setBusy(boolean busy) {
        runOnFx(() -> {
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
        });
    }

    private static void runOnFx(Runnable runnable) {
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
