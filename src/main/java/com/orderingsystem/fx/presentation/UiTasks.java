package com.orderingsystem.fx.presentation;

import com.orderingsystem.fx.presentation.ux.UiFeedback;
import com.orderingsystem.fx.presentation.ux.UserMessages;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Thao tác UI thống nhất — lỗi thân thiện (#9), xác nhận (#3), trạng thái (#1).
 */
public final class UiTasks {

    private UiTasks() {
    }

    public static void run(Runnable action) {
        run(action, null);
    }

    public static void run(Runnable action, Runnable onSuccess) {
        try {
            action.run();
            if (onSuccess != null) {
                onSuccess.run();
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public static void runWithStatus(String busyMessage, Runnable action, String successMessage) {
        try {
            UiFeedback.runWithFeedback(busyMessage, action, successMessage);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public static <T> T supply(Supplier<T> action) {
        try {
            return action.get();
        } catch (Exception ex) {
            showError(ex);
            return null;
        }
    }

    /** Heuristic #9 — Tóm tắt + gợi ý; chi tiết kỹ thuật ẩn trong phần mở rộng. */
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

    /** Heuristic #3 — Xác nhận trước thao tác không hoàn tác. */
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
