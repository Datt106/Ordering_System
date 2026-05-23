package com.orderingsystem.fx.framework;

import com.orderingsystem.fx.presentation.UiTasks;
import com.orderingsystem.fx.presentation.ux.UiFeedback;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Chạy công việc nặng ngoài JavaFX Application Thread (theo {@link Task} / {@link Platform#runLater}).
 */
public final class FxAsync {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
            runnable -> {
                Thread thread = new Thread(runnable, "ordering-fx-worker");
                thread.setDaemon(true);
                return thread;
            });

    private FxAsync() {
    }

    public static <T> void run(
            String busyMessage,
            Callable<T> backgroundWork,
            Consumer<T> onSuccessOnFxThread,
            String successMessage
    ) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return backgroundWork.call();
            }
        };

        UiFeedback.setBusy(true);
        UiFeedback.setStatus(busyMessage);

        task.setOnSucceeded(event -> Platform.runLater(() -> {
            try {
                T value = task.getValue();
                if (onSuccessOnFxThread != null) {
                    onSuccessOnFxThread.accept(value);
                }
                UiFeedback.setStatus(successMessage);
            } catch (Exception ex) {
                UiTasks.showError(ex);
            } finally {
                UiFeedback.setBusy(false);
            }
        }));

        task.setOnFailed(event -> Platform.runLater(() -> {
            UiFeedback.setBusy(false);
            Throwable ex = task.getException();
            if (ex instanceof Exception exception) {
                UiTasks.showError(exception);
            } else {
                UiTasks.showError(new RuntimeException(ex));
            }
        }));

        task.setOnCancelled(event -> Platform.runLater(() -> {
            UiFeedback.setBusy(false);
            UiFeedback.setStatus("Đã hủy.");
        }));

        EXECUTOR.execute(task);
    }

    public static void run(
            String busyMessage,
            Runnable backgroundWork,
            Runnable onSuccessOnFxThread,
            String successMessage
    ) {
        run(
                busyMessage,
                () -> {
                    backgroundWork.run();
                    return null;
                },
                ignored -> {
                    if (onSuccessOnFxThread != null) {
                        onSuccessOnFxThread.run();
                    }
                },
                successMessage
        );
    }
}
