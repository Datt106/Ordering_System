package com.orderingsystem.fx.presentation.site;

import com.orderingsystem.uc012.boundary.dto.SiteOrderDto;
import com.orderingsystem.fx.presentation.BaseViewController;
import com.orderingsystem.fx.presentation.UiTasks;
import com.orderingsystem.fx.presentation.ux.TableColumnLayout;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

public class SiteOrderConfirmController extends BaseViewController {

    private static final double[] COL_RATIOS = {0.22, 0.18, 0.18, 0.12, 0.30};

    @FXML
    private TableView<SiteOrderDto> table;
    @FXML
    private TableColumn<SiteOrderDto, String> orderIdCol;
    @FXML
    private TableColumn<SiteOrderDto, String> requestCol;
    @FXML
    private TableColumn<SiteOrderDto, String> codeCol;
    @FXML
    private TableColumn<SiteOrderDto, String> qtyCol;
    @FXML
    private TableColumn<SiteOrderDto, String> shipCol;

    @Override
    protected void onInit() {
        orderIdCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().orderId()));
        requestCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().requestId()));
        codeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().merchandiseCode()));
        qtyCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().quantityOrdered())));
        shipCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().deliveryMeans()));
        TableColumnLayout.bindProportionalColumns(table, COL_RATIOS, orderIdCol, requestCol, codeCol, qtyCol, shipCol);
        TableColumnLayout.bindEllipsisCellFactory(orderIdCol);
        bindTableScroll(table);
        Platform.runLater(this::refresh);
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    @FXML
    private void onConfirm() {
        SiteOrderDto selected = requireSelection();
        if (selected == null) {
            return;
        }
        String orderId = selected.orderId();
        UiTasks.<SiteOrderDto>runWithStatus(
                "Đang xác nhận...",
                () -> app.uc012().confirmOrder(orderId),
                order -> {
                    setScreenStatus("Đã xác nhận " + order.orderId());
                    UiTasks.showInfo("Đã xác nhận", "Đơn " + order.orderId() + " — " + order.merchandiseCode());
                    refresh();
                },
                "Đã cập nhật."
        );
    }

    @FXML
    private void onReject() {
        SiteOrderDto selected = requireSelection();
        if (selected == null) {
            return;
        }
        if (!UiTasks.confirm(
                "Từ chối đơn",
                "Từ chối đơn " + selected.orderId() + "?",
                "Đơn sẽ chuyển trạng thái Từ chối."
        )) {
            setScreenStatus("Đã hủy.");
            return;
        }
        String orderId = selected.orderId();
        UiTasks.<SiteOrderDto>runWithStatus(
                "Đang từ chối...",
                () -> app.uc012().rejectOrder(orderId),
                order -> {
                    setScreenStatus("Đã từ chối " + order.orderId());
                    UiTasks.showInfo("Đã từ chối", "Đơn " + order.orderId());
                    refresh();
                },
                "Đã cập nhật."
        );
    }

    private SiteOrderDto requireSelection() {
        SiteOrderDto selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setScreenStatus("Chọn đơn trong bảng.");
        }
        return selected;
    }

    private void refresh() {
        UiTasks.<List<SiteOrderDto>>runWithStatus(
                "Đang tải...",
                () -> app.uc012().listMyIncomingOrders(),
                items -> {
                    table.setItems(FXCollections.observableArrayList(items));
                    setScreenStatus(items.isEmpty()
                            ? "Không có đơn chờ."
                            : items.size() + " đơn chờ xác nhận.");
                },
                "Danh sách đã cập nhật."
        );
    }
}
