package com.orderingsystem.fx.presentation.ux;

import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;

/**
 * Cấu hình Spinner<Integer> cho phép nhập trực tiếp, có kiểm tra min/max.
 */
public final class SpinnerInputs {

    private SpinnerInputs() {
    }

    public static void configureIntegerSpinner(Spinner<Integer> spinner, int min, int max, int initialValue) {
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, initialValue);
        spinner.setValueFactory(valueFactory);
        spinner.setEditable(true);

        TextFormatter<String> formatter = new TextFormatter<>(change -> {
            String next = change.getControlNewText();
            if (next == null || next.isBlank()) {
                return change;
            }
            if (!next.matches("\\d+")) {
                return null;
            }
            try {
                int value = Integer.parseInt(next);
                return (value < min || value > max) ? null : change;
            } catch (NumberFormatException ex) {
                return null;
            }
        });
        spinner.getEditor().setTextFormatter(formatter);
        spinner.getEditor().setText(String.valueOf(initialValue));

        valueFactory.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                spinner.getEditor().setText(String.valueOf(newV));
            }
        });

        spinner.getEditor().setOnAction(event -> commitEditorText(spinner, min, max));
        spinner.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                commitEditorText(spinner, min, max);
            }
        });
    }

    private static void commitEditorText(Spinner<Integer> spinner, int min, int max) {
        String text = spinner.getEditor().getText();
        Integer current = spinner.getValueFactory().getValue();
        if (text == null || text.isBlank()) {
            spinner.getValueFactory().setValue(current != null ? current : min);
            return;
        }
        try {
            int parsed = Integer.parseInt(text.trim());
            int clamped = Math.max(min, Math.min(max, parsed));
            spinner.getValueFactory().setValue(clamped);
        } catch (NumberFormatException ex) {
            spinner.getValueFactory().setValue(current != null ? current : min);
        }
    }
}
