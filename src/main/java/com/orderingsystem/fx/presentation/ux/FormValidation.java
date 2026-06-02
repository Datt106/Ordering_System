package com.orderingsystem.fx.presentation.ux;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputControl;

import java.util.Arrays;

/**
 * Heuristic #5 — Giảm lỗi bằng ràng buộc trên form (disable nút khi dữ liệu chưa hợp lệ).
 */
public final class FormValidation {

    private FormValidation() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static void requireNonBlank(TextInputControl field, String message, Runnable onInvalid) {
        if (isBlank(field.getText())) {
            field.getStyleClass().remove("field-invalid");
            if (!field.getStyleClass().contains("field-invalid")) {
                field.getStyleClass().add("field-invalid");
            }
            if (onInvalid != null) {
                onInvalid.run();
            }
            throw new IllegalArgumentException(message);
        }
        clearInvalid(field);
    }

    public static void clearInvalid(Control field) {
        field.getStyleClass().remove("field-invalid");
    }

    public static void bindDisabledUntilFilled(Button button, TextInputControl... fields) {
        Observable[] deps = Arrays.stream(fields)
                .map(TextInputControl::textProperty)
                .toArray(Observable[]::new);
        var allFilled = Bindings.createBooleanBinding(
                () -> {
                    for (TextInputControl field : fields) {
                        if (isBlank(field.getText())) {
                            return false;
                        }
                    }
                    return true;
                },
                deps
        );
        button.disableProperty().bind(allFilled.not());
    }

    public static void bindDisabledUntilTableSelection(Button button, TableView<?> table) {
        button.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
    }
}
