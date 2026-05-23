package com.orderingsystem.fx.presentation.ux;

import javafx.beans.binding.Bindings;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.util.Callback;

/**
 * Bảng full width + cột % cố định (không kéo resize) — dùng chung Sales / Overseas / Site.
 */
public final class TableColumnLayout {

    private TableColumnLayout() {
    }

    @SafeVarargs
    public static <T> void bindProportionalColumns(TableView<T> table, double[] ratios, TableColumn<T, ?>... columns) {
        if (columns.length != ratios.length) {
            throw new IllegalArgumentException("Số cột và tỉ lệ phải khớp nhau.");
        }
        double sum = 0;
        for (double r : ratios) {
            sum += r;
        }
        if (Math.abs(sum - 1.0) > 0.001) {
            throw new IllegalArgumentException("Tổng tỉ lệ cột phải bằng 1.0, hiện tại: " + sum);
        }

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setMaxWidth(Double.MAX_VALUE);

        for (TableColumn<T, ?> col : columns) {
            col.setResizable(false);
            col.setReorderable(false);
        }

        for (int i = 0; i < columns.length; i++) {
            final double ratio = ratios[i];
            final double min = minWidthForRatio(ratio);
            TableColumn<T, ?> col = columns[i];
            col.prefWidthProperty().bind(
                    Bindings.createDoubleBinding(
                            () -> Math.max(min, table.getWidth() * ratio),
                            table.widthProperty()));
        }
    }

    public static <T, S> void bindEllipsisCellFactory(TableColumn<T, S> column) {
        column.setCellFactory(ellipsisCellFactory());
    }

    public static <T, S> Callback<TableColumn<T, S>, TableCell<T, S>> ellipsisCellFactory() {
        return column -> new TableCell<>() {
            @Override
            protected void updateItem(S item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    String text = String.valueOf(item);
                    setText(text);
                    setTooltip(new Tooltip(text));
                }
            }
        };
    }

    private static double minWidthForRatio(double ratio) {
        if (ratio <= 0.12) {
            return 48;
        }
        if (ratio <= 0.2) {
            return 64;
        }
        return 80;
    }
}
