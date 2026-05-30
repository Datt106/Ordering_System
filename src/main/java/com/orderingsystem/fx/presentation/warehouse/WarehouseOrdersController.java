package com.orderingsystem.fx.presentation.warehouse;

import com.orderingsystem.core.domain.OrderStatus;
import com.orderingsystem.uc013.boundary.dto.WarehouseOrderDto;
import com.orderingsystem.fx.presentation.BaseViewController;
import com.orderingsystem.fx.presentation.StatusLabels;
import com.orderingsystem.fx.presentation.ux.TableColumnLayout;
import com.orderingsystem.fx.presentation.UiTasks;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.util.List;

public class WarehouseOrdersController extends BaseViewController {

    private static final double[] COL_RATIOS = {0.18, 0.16, 0.12, 0.16, 0.10, 0.28};

    @FXML
    private ComboBox<OrderStatus> statusFilter;
    @FXML
    private TextField siteFilter;
    @FXML
    private TextField merchandiseFilter;
    @FXML
    private TableView<WarehouseOrderDto> table;
    @FXML
    private TableColumn<WarehouseOrderDto, String> orderIdCol;
    @FXML
    private TableColumn<WarehouseOrderDto, String> requestCol;
    @FXML
    private TableColumn<WarehouseOrderDto, String> siteCol;
    @FXML
    private TableColumn<WarehouseOrderDto, String> codeCol;
    @FXML
    private TableColumn<WarehouseOrderDto, String> qtyCol;
    @FXML
    private TableColumn<WarehouseOrderDto, String> statusCol;

    @Override
    protected void onInit() {
        statusFilter.setItems(FXCollections.observableArrayList(
                null,
                OrderStatus.CHO_GUI,
                OrderStatus.DA_GUI,
                OrderStatus.DA_XAC_NHAN,
                OrderStatus.TU_CHOI,
                OrderStatus.DA_NHAP_KHO,
                OrderStatus.SAI_LECH
        ));
        statusFilter.setConverter(orderStatusConverter());
        statusFilter.getSelectionModel().selectFirst();

        orderIdCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().orderId()));
        requestCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().requestId()));
        siteCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().siteCode()));
        codeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().merchandiseCode()));
        qtyCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().quantityOrdered())));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(StatusLabels.orderStatus(c.getValue().status())));
        TableColumnLayout.bindProportionalColumns(table, COL_RATIOS, orderIdCol, requestCol, siteCol, codeCol, qtyCol, statusCol);
        TableColumnLayout.bindEllipsisCellFactory(orderIdCol);
        TableColumnLayout.bindEllipsisCellFactory(statusCol);

        bindEmptyTable(table, "Chưa có đơn hàng — hoàn tất luồng Sales → Overseas → Site trước.");
        Platform.runLater(this::onSearch);
    }

    @FXML
    private void onSearch() {
        OrderStatus status = statusFilter.getValue();
        String site = siteFilter.getText();
        String merchandise = merchandiseFilter.getText();
        UiTasks.<List<WarehouseOrderDto>>runWithStatus(
                "Đang tìm...",
                () -> app.uc013().listOrders(status, site, merchandise),
                this::applyOrders,
                "Danh sách đã cập nhật."
        );
    }

    private void applyOrders(List<WarehouseOrderDto> items) {
        table.setItems(FXCollections.observableArrayList(items));
        setScreenStatus(items.isEmpty() ? "Không có đơn khớp bộ lọc." : "Tìm thấy " + items.size() + " đơn.");
    }

    private static StringConverter<OrderStatus> orderStatusConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(OrderStatus status) {
                return status == null ? "Tất cả trạng thái" : StatusLabels.orderStatus(status);
            }

            @Override
            public OrderStatus fromString(String string) {
                return null;
            }
        };
    }
}
