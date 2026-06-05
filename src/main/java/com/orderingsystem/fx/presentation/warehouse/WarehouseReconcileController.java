package com.orderingsystem.fx.presentation.warehouse;

import com.orderingsystem.core.domain.OrderStatus;
import com.orderingsystem.uc013.boundary.dto.WarehouseOrderDto;
import com.orderingsystem.uc014.boundary.dto.WarehouseReconcileResultDto;
import com.orderingsystem.fx.presentation.BaseViewController;
import com.orderingsystem.fx.presentation.StatusLabels;
import com.orderingsystem.fx.presentation.UiTasks;
import com.orderingsystem.fx.presentation.ux.TableColumnLayout;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;

public class WarehouseReconcileController extends BaseViewController {

    // Tỷ lệ cho bảng đối chiếu (5 cột): Mã đơn (28%), Site (25%), Hàng (25%), SL đặt (8%), Trạng thái (14%)
    private static final double[] COL_RATIOS = {0.28, 0.25, 0.25, 0.08, 0.14};
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private TextField actualField;
    @FXML private TextField timeField; // Ô nhập thời gian mới
    @FXML private Button reconcileButton;
    @FXML private TableView<WarehouseOrderDto> table;
    
    @FXML private TableColumn<WarehouseOrderDto, String> orderIdCol;
    @FXML private TableColumn<WarehouseOrderDto, String> siteCol;
    @FXML private TableColumn<WarehouseOrderDto, String> codeCol;
    @FXML private TableColumn<WarehouseOrderDto, String> orderedCol;
    @FXML private TableColumn<WarehouseOrderDto, String> statusCol;

    @Override
    protected void onInit() {
        actualField.setText("0");
        // Mặc định điền sẵn giờ hiện tại cho tiện
        timeField.setText(LocalDateTime.now().format(TIME_FORMATTER));
        
        orderIdCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().orderId()));
        
        // Hiển thị Mã - Tên
        siteCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().siteCode() + (c.getValue().siteName() != null ? " - " + c.getValue().siteName() : "")));
        codeCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().merchandiseCode() + (c.getValue().merchandiseName() != null ? " - " + c.getValue().merchandiseName() : "")));
                
        orderedCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().quantityOrdered())));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(StatusLabels.orderStatus(c.getValue().status())));
        
        TableColumnLayout.bindProportionalColumns(table, COL_RATIOS, orderIdCol, siteCol, codeCol, orderedCol, statusCol);
        TableColumnLayout.bindEllipsisCellFactory(orderIdCol);
        TableColumnLayout.bindEllipsisCellFactory(siteCol);
        TableColumnLayout.bindEllipsisCellFactory(codeCol);

        table.getSelectionModel().selectedItemProperty().addListener((obs, o, row) -> {
            reconcileButton.setDisable(row == null);
            if (row != null) {
                actualField.setText(String.valueOf(row.quantityOrdered()));
                timeField.setText(LocalDateTime.now().format(TIME_FORMATTER)); // Reset giờ khi bấm chọn dòng khác
            } else {
                actualField.setText("0");
            }
        });
        reconcileButton.setDisable(true);

        bindTableScroll(table);
        Platform.runLater(this::refresh);
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    @FXML
    private void onReconcile() {
        WarehouseOrderDto selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setScreenStatus("Chọn đơn trong bảng.");
            return;
        }
        
        int actual;
        try {
            actual = Integer.parseInt(actualField.getText().trim());
            if (actual < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            UiTasks.showError(new IllegalArgumentException("Vui lòng nhập số lượng hợp lệ."));
            return;
        }
        
        // Xử lý lấy thời gian từ ô nhập liệu
        Instant actualTime;
        try {
            LocalDateTime ldt = LocalDateTime.parse(timeField.getText().trim(), TIME_FORMATTER);
            actualTime = ldt.atZone(ZoneId.systemDefault()).toInstant();
        } catch (Exception e) {
            UiTasks.showError(new IllegalArgumentException("Sai định dạng thời gian. Vui lòng nhập: dd/MM/yyyy HH:mm"));
            return;
        }
        
        String orderId = selected.orderId();
        UiTasks.<WarehouseReconcileResultDto>runWithStatus(
                "Đang ghi nhận...",
                () -> app.uc014().recordInbound(orderId, actual, actualTime), // Truyền thêm actualTime
                result -> {
                    setScreenStatus("Đơn " + result.orderId() + ": "
                            + StatusLabels.orderStatus(result.status())
                            + " (lệch " + result.quantityDiff() + ")");
                    UiTasks.showInfo(
                            "Đã ghi nhận",
                            "Đặt: " + result.orderedQuantity()
                                    + " · Thực nhận: " + result.actualQuantity()
                                    + " · " + StatusLabels.orderStatus(result.status())
                    );
                    refresh();
                },
                "Đối chiếu hoàn tất."
        );
    }

    private void refresh() {
        UiTasks.<List<WarehouseOrderDto>>runWithStatus(
                "Đang tải...",
                () -> app.uc013().listOrders(null, null, null).stream()
                        .filter(o -> EnumSet.of(OrderStatus.DA_GUI, OrderStatus.DA_XAC_NHAN).contains(o.status()))
                        .toList(),
                items -> {
                    table.setItems(FXCollections.observableArrayList(items));
                    reconcileButton.setDisable(table.getSelectionModel().getSelectedItem() == null);
                    setScreenStatus(items.isEmpty()
                            ? "Không có đơn Đã gửi/Đã xác nhận."
                            : items.size() + " đơn — chọn dòng, nhập SL thực, Ghi nhận.");
                },
                "Danh sách đã cập nhật."
        );
    }
}