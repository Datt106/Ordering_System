package com.orderingsystem.fx.presentation;

import com.orderingsystem.fx.app.AppContext;
import com.orderingsystem.fx.presentation.ux.EmptyStates;
import com.orderingsystem.fx.presentation.ux.FormValidation;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputControl;

/**
 * Lớp cơ sở — hỗ trợ trạng thái màn hình & empty state (Nielsen #1, #8).
 */
public abstract class BaseViewController implements ViewController {

    protected AppContext app;

    @FXML
    private Label screenStatusLabel;

    @FXML
    private Label emptyStateLabel;

    @Override
    public void init(AppContext appContext) {
        this.app = appContext;
        onInit();
    }

    protected void onInit() {
    }

    protected void setScreenStatus(String message) {
        if (screenStatusLabel != null) {
            screenStatusLabel.setText(message);
        }
    }

    protected void bindEmptyTable(TableView<?> table, String emptyMessage) {
        EmptyStates.bindTable(table, emptyStateLabel, emptyMessage);
    }

    protected void validateRequired(TextInputControl field, String message) {
        FormValidation.requireNonBlank(field, message, () -> setScreenStatus(message));
    }
}
