package com.orderingsystem.fx.presentation.overseas;

import com.orderingsystem.core.domain.OrderStatus;
import com.orderingsystem.uc002.boundary.dto.ImportRequestDto;
import com.orderingsystem.uc008.boundary.dto.OrderDispatchResultDto;
import com.orderingsystem.fx.presentation.BaseViewController;
import com.orderingsystem.fx.presentation.StatusLabels;
import com.orderingsystem.fx.presentation.UiTasks;
import com.orderingsystem.fx.presentation.ux.TableColumnLayout;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OverseasOrderDispatchController extends BaseViewController {

    private static final double[] REQ_COL_RATIOS = {0.45, 0.55};
    private static final double[] ORDER_COL_RATIOS = {0.22, 0.14, 0.18, 0.12, 0.34};
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault());

    @FXML
    private TableView<ImportRequestDto> requestTable;
    @FXML
    private TableColumn<ImportRequestDto, String> reqIdCol;
    @FXML
    private TableColumn<ImportRequestDto, String> reqByCol;
    @FXML
    private TextArea summaryArea;
    @FXML
    private TableView<OrderDispatchResultDto.OrderDispatchLineDto> ordersTable;
    @FXML
    private TableColumn<OrderDispatchResultDto.OrderDispatchLineDto, String> orderIdCol;
    @FXML
    private TableColumn<OrderDispatchResultDto.OrderDispatchLineDto, String> siteCol;
    @FXML
    private TableColumn<OrderDispatchResultDto.OrderDispatchLineDto, String> codeCol;
    @FXML
    private TableColumn<OrderDispatchResultDto.OrderDispatchLineDto, String> qtyCol;
    @FXML
    private TableColumn<OrderDispatchResultDto.OrderDispatchLineDto, String> statusCol;

    @Override
    protected void onInit() {
        reqIdCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().requestId()));
        reqByCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().processedBy() != null ? c.getValue().processedBy() : "—"));
        TableColumnLayout.bindProportionalColumns(requestTable, REQ_COL_RATIOS, reqIdCol, reqByCol);

        orderIdCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().orderId()));
        siteCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().siteCode()));
        codeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().merchandiseCode()));
        qtyCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().quantityOrdered())));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(StatusLabels.orderStatus(c.getValue().status())));
        TableColumnLayout.bindProportionalColumns(ordersTable, ORDER_COL_RATIOS, orderIdCol, siteCol, codeCol, qtyCol, statusCol);
        TableColumnLayout.bindEllipsisCellFactory(orderIdCol);
        TableColumnLayout.bindEllipsisCellFactory(statusCol);

        bindEmptyTable(requestTable, "Không có yêu cầu — tách đơn trước khi gửi.");
        Platform.runLater(this::refresh);
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    @FXML
    private void onPreview() {
        runDispatch(false);
    }

    @FXML
    private void onDispatch() {
        ImportRequestDto selected = requestTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setScreenStatus("Chọn yêu cầu trong bảng.");
            return;
        }
        if (!UiTasks.confirm(
                "Gửi đơn hàng",
                "Gửi đơn con của " + selected.requestId() + " tới các Site?",
                "Site sẽ thấy đơn ở trạng thái Đã gửi và có thể xác nhận."
        )) {
            setScreenStatus("Đã hủy gửi đơn.");
            return;
        }
        runDispatch(true);
    }

    private void runDispatch(boolean send) {
        ImportRequestDto selected = requestTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setScreenStatus("Chọn yêu cầu trong bảng.");
            return;
        }
        String requestId = selected.requestId();
        UiTasks.<OrderDispatchResultDto>runWithStatus(
                send ? "Đang gửi đơn..." : "Đang xem trước...",
                () -> send ? app.uc008().dispatchOrders(requestId) : app.uc008().previewToSend(requestId),
                result -> applyDispatchResult(result, send),
                send ? "Đã gửi đơn." : "Xem trước hoàn tất."
        );
    }

    private void applyDispatchResult(OrderDispatchResultDto result, boolean sent) {
        ordersTable.setItems(FXCollections.observableArrayList(result.lines()));
        StringBuilder sb = new StringBuilder();
        sb.append("Yêu cầu: ").append(result.requestId()).append('\n');
        sb.append("Tổng đơn: ").append(result.totalOrders()).append('\n');
        if (sent) {
            sb.append("Đã gửi: ").append(result.sentOrders()).append('\n');
            if (result.sentAt() != null) {
                sb.append("Lúc: ").append(DT.format(result.sentAt())).append('\n');
            }
            UiTasks.showInfo("Đã gửi đơn", result.sentOrders() + " đơn con đã gửi tới Site.");
            refresh();
        } else {
            long ready = result.lines().stream().filter(l -> l.status() == OrderStatus.CHO_GUI).count();
            sb.append("Sẵn sàng gửi: ").append(ready).append(" đơn\n");
        }
        summaryArea.setText(sb.toString());
        setScreenStatus(sent ? "Đã gửi " + result.sentOrders() + " đơn." : "Xem trước " + result.lines().size() + " dòng.");
    }

    private void refresh() {
        UiTasks.<List<ImportRequestDto>>runWithStatus(
                "Đang tải...",
                () -> app.uc005().listProcessingRequests(),
                items -> {
                    requestTable.setItems(FXCollections.observableArrayList(items));
                    setScreenStatus(items.isEmpty()
                            ? "Chưa có yêu cầu."
                            : items.size() + " yêu cầu — chọn yêu cầu đã tách đơn.");
                },
                "Danh sách đã cập nhật."
        );
    }
}
