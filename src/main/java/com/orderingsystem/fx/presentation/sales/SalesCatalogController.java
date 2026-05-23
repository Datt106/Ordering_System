package com.orderingsystem.fx.presentation.sales;

import com.orderingsystem.uc001.dto.StandardMerchandiseDto;
import com.orderingsystem.fx.presentation.BaseViewController;
import com.orderingsystem.fx.presentation.UiTasks;
import com.orderingsystem.fx.presentation.ux.FormValidation;
import com.orderingsystem.fx.presentation.ux.TableColumnLayout;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.List;

public class SalesCatalogController extends BaseViewController {

    private static final double[] CATALOG_COL_RATIOS = {0.15, 0.35, 0.50};

    @FXML
    private TableView<StandardMerchandiseDto> table;
    @FXML
    private TableColumn<StandardMerchandiseDto, String> codeCol;
    @FXML
    private TableColumn<StandardMerchandiseDto, String> nameCol;
    @FXML
    private TableColumn<StandardMerchandiseDto, String> descCol;
    @FXML
    private TextField codeField;
    @FXML
    private TextField nameField;
    @FXML
    private TextArea descArea;
    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;

    @Override
    protected void onInit() {
        codeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().merchandiseCode()));
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().merchandiseName()));
        descCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().description() != null ? c.getValue().description() : ""));

        TableColumnLayout.bindProportionalColumns(table, CATALOG_COL_RATIOS, codeCol, nameCol, descCol);
        TableColumnLayout.bindEllipsisCellFactory(nameCol);
        TableColumnLayout.bindEllipsisCellFactory(descCol);

        table.getSelectionModel().selectedItemProperty().addListener((obs, o, row) -> {
            if (row != null) {
                codeField.setText(row.merchandiseCode());
                nameField.setText(row.merchandiseName());
                descArea.setText(row.description() != null ? row.description() : "");
                setScreenStatus("Đang sửa: " + row.merchandiseCode());
            }
        });
        FormValidation.bindDisabledUntilFilled(addButton, codeField, nameField);
        FormValidation.bindDisabledUntilFilled(updateButton, codeField, nameField);
        bindEmptyTable(table, "Chưa có mặt hàng trong danh mục. Dùng form bên dưới để thêm mã mới.");
        refresh();
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    @FXML
    private void onAdd() {
        try {
            validateRequired(codeField, "Nhập mã hàng (*).");
            validateRequired(nameField, "Nhập tên mặt hàng (*).");
        } catch (IllegalArgumentException ex) {
            UiTasks.showError(ex);
            return;
        }
        String code = codeField.getText().trim();
        String name = nameField.getText().trim();
        String desc = descArea.getText();
        UiTasks.runWithStatus(
                "Đang thêm mặt hàng…",
                () -> {
                    app.catalog().registerMerchandise(code, name, desc);
                    return code;
                },
                savedCode -> {
                    setScreenStatus("Đã thêm: " + savedCode);
                    UiTasks.showInfo("Đã lưu", "Mặt hàng mới có trong danh mục chuẩn.");
                    refresh();
                },
                "Danh mục đã cập nhật."
        );
    }

    @FXML
    private void onUpdate() {
        try {
            validateRequired(codeField, "Chọn hoặc nhập mã hàng (*).");
            validateRequired(nameField, "Nhập tên mặt hàng (*).");
        } catch (IllegalArgumentException ex) {
            UiTasks.showError(ex);
            return;
        }
        String code = codeField.getText().trim();
        String name = nameField.getText().trim();
        String desc = descArea.getText();
        UiTasks.runWithStatus(
                "Đang cập nhật…",
                () -> {
                    app.catalog().updateMerchandise(code, name, desc);
                    return code;
                },
                savedCode -> {
                    setScreenStatus("Đã cập nhật: " + savedCode);
                    UiTasks.showInfo("Đã lưu", "Thông tin mặt hàng đã được cập nhật.");
                    refresh();
                },
                "Danh mục đã cập nhật."
        );
    }

    @FXML
    private void onDelete() {
        try {
            validateRequired(codeField, "Chọn mặt hàng cần xóa trong bảng hoặc nhập mã.");
        } catch (IllegalArgumentException ex) {
            UiTasks.showError(ex);
            return;
        }
        String code = codeField.getText().trim();
        if (!UiTasks.confirmDelete("Mặt hàng: " + code)) {
            setScreenStatus("Đã hủy xóa.");
            return;
        }
        UiTasks.runWithStatus(
                "Đang xóa…",
                () -> {
                    app.catalog().deleteMerchandise(code);
                    return code;
                },
                deletedCode -> {
                    codeField.clear();
                    nameField.clear();
                    descArea.clear();
                    setScreenStatus("Đã xóa: " + deletedCode);
                    UiTasks.showInfo("Đã xóa", "Mã " + deletedCode + " đã được gỡ khỏi danh mục.");
                    refresh();
                },
                "Danh mục đã cập nhật."
        );
    }

    private void refresh() {
        UiTasks.runWithStatus(
                "Đang tải danh mục…",
                () -> app.catalog().listAll(),
                this::applyCatalogList,
                "Danh mục đã tải."
        );
    }

    private void applyCatalogList(List<StandardMerchandiseDto> items) {
        table.setItems(FXCollections.observableArrayList(items));
        setScreenStatus(items.isEmpty()
                ? "Danh mục trống — thêm mặt hàng đầu tiên."
                : "Hiển thị " + items.size() + " mặt hàng.");
    }
}
