package com.orderingsystem.fx.presentation.ux;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;

/**
 * Bọc nội dung màn hình trong {@link ScrollPane} để cuộn dọc khi nội dung dài.
 */
public final class ScrollSupport {

    private ScrollSupport() {
    }

    public static ScrollPane wrapScreen(Node content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(false);
        scroll.setPannable(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.getStyleClass().add("screen-scroll");
        return scroll;
    }
}
