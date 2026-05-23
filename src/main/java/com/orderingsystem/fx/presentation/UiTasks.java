package com.orderingsystem.fx.presentation;

import com.orderingsystem.fx.framework.FxAsync;
import com.orderingsystem.fx.presentation.ux.UiFeedback;
import com.orderingsystem.fx.presentation.ux.UserMessages;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Thao tác UI — dialog/xác nhận trên FX thread; tác vụ nặng qua {@link FxAsync} + {@link javafx.concurrent.Task}.
 */
public final class UiTasks {

    private UiTasks() {
    }

    /** Chạy đồng bộ trên FX thread (chỉ dùng cho thao tác UI thuần, không gọi DB). */
    public static void runOnFxThread(Runnable action) {
        runOnFxThread(action, null);
    }

    public static void runOnFxThread(Runnable action, Runnable onSuccess) {
        if (Platform.isFxApplicationThread()) {
            executeOnFx(action, onSuccess);
        } else {
            Platform.runLater(() -> executeOnFx(action, onSuccess));
        }
    }

    private static void executeOnFx(Runnable action, Runnable onSuccess) {
        try {
            action.run();
            if (onSuccess != null) {
                onSuccess.run();
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    /**
     * Tác vụ nền (DB/service) + cập nhật UI trên Application Thread.
     */
    public static <T> void runWithStatus(
            String busyMessage,
            Callable<T> backgroundWork,
            Consumer<T> onSuccessOnFxThread,
            String successMessage
    ) {
        FxAsync.run(busyMessage, backgroundWork, onSuccessOnFxThread, successMessage);
    }

    public static void runWithStatus(
            String busyMessage,
            Runnable backgroundWork,
            Runnable onSuccessOnFxThread,
            String successMessage
    ) {
        FxAsync.run(busyMessage, backgroundWork, onSuccessOnFxThread, successMessage);
    }

    /**
     * Chỉ chạy trên FX thread — dùng khi thao tác đã nằm trên FX thread và không gọi service/DB.
     */
    public static void runWithStatusOnFxThread(String busyMessage, Runnable fxAction, String successMessage) {
        runOnFxThread(() -> {
            UiFeedback.setBusy(true);
            UiFeedback.setStatus(busyMessage);
            try {
                fxAction.run();
                UiFeedback.setStatus(successMessage);
            } finally {
                UiFeedback.setBusy(false);
            }
        });
    }

    @Deprecated
    public static void run(Runnable action) {
        runOnFxThread(action);
    }

    @Deprecated
    public static void run(Runnable action, Runnable onSuccess) {
        runOnFxThread(action, onSuccess);
    }

    public static <T> T supply(Supplier<T> action) {
        try {
            return action.get();
        } catch (Exception ex) {
            showError(ex);
            return null;
        }
    }

    public static void showError(Exception ex) {
        UserMessages.FriendlyError friendly = UserMessages.from(ex);
        UiFeedback.setStatus("Lỗi: " + friendly.summary());

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Không thực hiện được");
        alert.setHeaderText(friendly.summary());
        alert.setContentText(friendly.recoveryHint());

        TextArea technical = new TextArea(formatTechnicalDetail(ex));
        technical.setEditable(false);
        technical.setWrapText(true);
        technical.setPrefRowCount(6);
        alert.getDialogPane().setExpandableContent(technical);

        alert.showAndWait();
    }

    public static void showInfo(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thành công");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
        UiFeedback.setStatus(header);
    }

    public static boolean confirm(String title, String header, String detail) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(detail);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static boolean confirmDelete(String itemDescription) {
        return confirm(
                "Xác nhận xóa",
                "Bạn có chắc muốn xóa?",
                itemDescription + "\n\nThao tác này không thể hoàn tác."
        );
    }

    public static Label statusLabel(String text, boolean error) {
        Label label = new Label(text);
        label.getStyleClass().add(error ? "status-error" : "status-ok");
        label.setWrapText(true);
        return label;
    }

    private static String formatTechnicalDetail(Exception ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        return "Chi tiết kỹ thuật (dành cho hỗ trợ IT):\n\n" + sw;
    }
}
