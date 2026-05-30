package com.orderingsystem.fx.presentation;

import com.orderingsystem.fx.app.AppContext;
import com.orderingsystem.fx.presentation.ux.EmptyStates;
import com.orderingsystem.fx.presentation.ux.FormValidation;
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
