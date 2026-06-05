package com.orderingsystem.fx.presentation;

import com.orderingsystem.fx.app.AppContext;
import com.orderingsystem.fx.presentation.ux.EmptyStates;
import com.orderingsystem.fx.presentation.ux.FormValidation;
import com.orderingsystem.fx.presentation.ux.TableColumnLayout;
import javafx.beans.binding.Bindings;
import javafx.scene.layout.Region;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputControl;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller FXML chuẩn: {@link Initializable#initialize} cho UI, {@link #init} cho dependency injection.
 */
public abstract class BaseViewController implements ViewController, Initializable {

    protected AppContext app;

    @FXML
    protected Label screenStatusLabel;

    @FXML
    protected Label emptyStateLabel;

    @Override
    public final void initialize(URL location, ResourceBundle resources) {
        initializeView(location, resources);
    }

    /**
     * Gọi tự động bởi {@link javafx.fxml.FXMLLoader} sau khi inject {@code @FXML} — chỉ thiết lập UI.
     */
    protected void initializeView(URL location, ResourceBundle resources) {
    }

    @Override
    public final void init(AppContext appContext) {
        this.app = appContext;
        onInit();
    }

    /** Gọi sau {@link #init} khi đã có {@link AppContext}. */
    protected void onInit() {
    }

    protected void setScreenStatus(String message) {
        // Trạng thái màn hình không hiển thị trên UI.
    }

    protected void bindEmptyTable(TableView<?> table, String emptyMessage) {
        if (emptyStateLabel != null) {
            EmptyStates.bindTable(table, emptyStateLabel, emptyMessage);
        }
    }

    protected void bindTableScroll(TableView<?> table, Region container) {
        double height = container.getMinHeight() > 0
                ? container.getMinHeight()
                : TableColumnLayout.DEFAULT_TABLE_HEIGHT;
        container.setPrefHeight(height);
        container.setMinHeight(height);
        container.setMaxHeight(height);
        TableColumnLayout.constrainHeight(table, height);
    }

    protected void bindTableScroll(TableView<?> table) {
        TableColumnLayout.constrainHeight(table);
    }

    protected void bindTableScroll(TableView<?> table, double height) {
        TableColumnLayout.constrainHeight(table, height);
    }

    /**
     * Khung bảng kết quả căn giữa trong {@code area}, mỗi cạnh cách viền {@code marginRatio}
     * (vd. 0.05 → bảng chiếm 90% chiều ngang và chiều dọc vùng chứa).
     */
    protected void bindResultTableInsets(Region tableFrame, Region area, double marginRatio) {
        double sizeRatio = 1.0 - 2 * marginRatio;
        tableFrame.prefWidthProperty().bind(Bindings.createDoubleBinding(
                () -> Math.max(0, area.getWidth() * sizeRatio),
                area.widthProperty()));
        tableFrame.maxWidthProperty().bind(tableFrame.prefWidthProperty());
        tableFrame.prefHeightProperty().bind(Bindings.createDoubleBinding(
                () -> Math.max(0, area.getHeight() * sizeRatio),
                area.heightProperty()));
        tableFrame.maxHeightProperty().bind(tableFrame.prefHeightProperty());
    }

    protected void validateRequired(TextInputControl field, String message) {
        FormValidation.requireNonBlank(field, message, () -> setScreenStatus(message));
    }
}
