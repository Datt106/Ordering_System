package com.orderingsystem.fx.presentation.site;

import com.orderingsystem.uc006.boundary.dto.InventoryQueryDto;
import com.orderingsystem.fx.presentation.BaseViewController;
import com.orderingsystem.fx.presentation.UiTasks;
import com.orderingsystem.fx.presentation.ux.TableColumnLayout;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

public class SiteInventoryResponseController extends BaseViewController {

    private static final double[] QUERY_COL_RATIOS = {0.40, 0.35, 0.25};

    @FXML private TableView<InventoryQueryDto> table;
    @FXML private TableColumn<InventoryQueryDto, String> requestCol;
    @FXML private TableColumn<InventoryQueryDto, String> codeCol;
    @FXML private TableColumn<InventoryQueryDto, String> unitCol;
    
    // Đã thay đổi từ Spinner sang TextField
    @FXML private TextField stockField;
    @FXML private Button respondButton;

    @Override
    protected void onInit() {
        stockField.setText("0");
        requestCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().requestId()));
        codeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().merchandiseCode()));
        unitCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().unit()));

        TableColumnLayout.bindProportionalColumns(table, QUERY_COL_RATIOS, requestCol, codeCol, unitCol);
        TableColumnLayout.bindEllipsisCellFactory(requestCol);
        TableColumnLayout.bindEllipsisCellFactory(codeCol);
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) ->
                respondButton.setDisable(selected == null));
        respondButton.setDisable(true);

        bindTableScroll(table);
        refresh();
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    @FXML
    private void onRespond() {
        InventoryQueryDto selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setScreenStatus("Chọn một dòng truy vấn trong bảng.");
            return;
        }
        
        int qty;
        try {
            qty = Integer.parseInt(stockField.getText().trim());
        } catch (NumberFormatException e) {
            UiTasks.showError(new IllegalArgumentException("Vui lòng nhập số lượng hợp lệ."));
            return;
        }
        
        String queryId = selected.queryId();
        String merchandiseCode = selected.merchandiseCode();
        String unit = selected.unit();
        UiTasks.runWithStatus(
                "Đang gửi phản hồi…",
                () -> {
                    app.uc011().respond(queryId, qty);
                    return qty;
                },
                ignored -> {
                    setScreenStatus("Đã phản hồi " + merchandiseCode + ": tồn " + qty);
                    UiTasks.showInfo("Đã gửi", "Tồn kho " + qty + " " + unit + " cho " + merchandiseCode);
                    refresh();
                },
                "Sẵn sàng nhận truy vấn mới."
        );
    }

    private void refresh() {
        UiTasks.runWithStatus(
                "Đang tải truy vấn…",
                () -> app.uc011().listMyPendingQueries(),
                this::applyQueryList,
                "Danh sách đã cập nhật."
        );
    }

    private void applyQueryList(List<InventoryQueryDto> items) {
        table.setItems(FXCollections.observableArrayList(items));
        respondButton.setDisable(table.getSelectionModel().getSelectedItem() == null);
        setScreenStatus(items.isEmpty()
                ? "Không còn truy vấn chờ phản hồi."
                : items.size() + " truy vấn chờ — chọn dòng, nhập tồn, bấm Gửi phản hồi.");
    }
}