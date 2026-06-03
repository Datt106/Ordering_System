package com.orderingsystem.fx.presentation.ux;

import javafx.scene.layout.Region;

/**
 * Hiển thị / ẩn panel nhập liệu khi bấm Thêm hoặc Sửa.
 */
public final class FormPanels {

    public enum Mode {
        ADD,
        EDIT
    }

    private FormPanels() {
    }

    public static void open(Region panel) {
        panel.setVisible(true);
        panel.setManaged(true);
    }

    public static void close(Region panel) {
        panel.setVisible(false);
        panel.setManaged(false);
    }

    public static boolean isOpen(Region panel) {
        return panel.isVisible();
    }
}
