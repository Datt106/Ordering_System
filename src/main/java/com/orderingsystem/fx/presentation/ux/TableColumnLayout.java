package com.orderingsystem.fx.presentation.ux;

import javafx.beans.binding.Bindings;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.util.Callback;

/**
 * Bảng full width + cột % cố định — dùng binding JavaFX và CSS {@code -fx-text-overrun: ellipsis}.
 */
public final class TableColumnLayout {

    private static final double TABLE_CELL_SIZE = 48;

    private TableColumnLayout() {
    }

    @SafeVarargs
    public static <T> void bindProportionalColumns(TableView<T> table, double[] ratios, TableColumn<T, ?>... columns) {
        if (columns.length != ratios.length) {
            throw new IllegalArgumentException("Số cột và tỉ lệ phải khớp nhau.");
        }
        double sum = 0;
        for (double ratio : ratios) {
            sum += ratio;
        }
        if (Math.abs(sum - 1.0) > 0.001) {
            throw new IllegalArgumentException("Tổng tỉ lệ cột phải bằng 1.0, hiện tại: " + sum);
        }

        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setMaxWidth(Double.MAX_VALUE);

        for (TableColumn<T, ?> col : columns) {
            col.setResizable(false);
            col.setReorderable(false);
            col.setSortable(true);
            col.getStyleClass().add("col-proportional");
        }

        for (int i = 0; i < columns.length; i++) {
            final double ratio = ratios[i];
            final double min = minWidthForRatio(ratio);
            TableColumn<T, ?> col = columns[i];
            col.prefWidthProperty().bind(
                    Bindings.createDoubleBinding(
                            () -> Math.max(min, table.getWidth() * ratio),
                            table.widthProperty()));
            col.minWidthProperty().bind(col.prefWidthProperty());
            col.maxWidthProperty().bind(col.prefWidthProperty());
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

    public static final double DEFAULT_TABLE_HEIGHT = 240;
    public static final double COMPACT_TABLE_HEIGHT = 180;

    /**
     * Chiều cao cố định — bảng cuộn nội bộ, không giãn theo số dòng.
     */
    public static void constrainHeight(TableView<?> table, double height) {
        table.setFixedCellSize(TABLE_CELL_SIZE);
        table.setPrefHeight(height);
        table.setMinHeight(height);
        table.setMaxHeight(height);
    }

    public static void constrainHeight(TableView<?> table) {
        constrainHeight(table, DEFAULT_TABLE_HEIGHT);
    }

    /**
     * @deprecated dùng {@link #constrainHeight(TableView, double)}
     */
    @Deprecated
    public static void bindVerticalScroll(TableView<?> table, Region container) {
        double height = container.getMinHeight() > 0
                ? container.getMinHeight()
                : DEFAULT_TABLE_HEIGHT;
        container.setPrefHeight(height);
        container.setMinHeight(height);
        container.setMaxHeight(height);
        constrainHeight(table, height);
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
