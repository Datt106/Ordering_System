package com.orderingsystem.fx.presentation.ux;

import javafx.beans.binding.Bindings;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

/**
 * Heuristic #8 — Trạng thái rỗng rõ ràng thay vì bảng trống gây nhầm lẫn.
 */
public final class EmptyStates {

    private EmptyStates() {
    }

    public static void bindTable(TableView<?> table, Label emptyLabel, String emptyMessage) {
        if (emptyLabel == null) {
            return;
        }
        emptyLabel.setText(emptyMessage);
        emptyLabel.visibleProperty().bind(Bindings.createBooleanBinding(
                () -> table.getItems() == null || table.getItems().isEmpty(),
                table.getItems()
        ));
        emptyLabel.managedProperty().bind(emptyLabel.visibleProperty());
    }
}
