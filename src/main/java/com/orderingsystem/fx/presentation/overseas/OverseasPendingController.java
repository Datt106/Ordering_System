package com.orderingsystem.fx.presentation.overseas;

import com.orderingsystem.uc002.dto.ImportRequestDto;
import com.orderingsystem.fx.presentation.BaseViewController;
import com.orderingsystem.fx.presentation.StatusLabels;
import com.orderingsystem.fx.presentation.UiTasks;
import com.orderingsystem.fx.presentation.ux.TableColumnLayout;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class OverseasPendingController extends BaseViewController {

    /** Mã YC 35% · Tạo lúc 35% · Người tạo 30% */
    private static final double[] PENDING_COL_RATIOS = {0.35, 0.35, 0.30};

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault());

    @FXML
    private TableView<ImportRequestDto> pendingTable;
    @FXML
    private TableColumn<ImportRequestDto, String> idCol;
    @FXML
    private TableColumn<ImportRequestDto, String> createdCol;
    @FXML
    private TableColumn<ImportRequestDto, String> byCol;
    @FXML
    private TextArea detailArea;

    @Override
    protected void onInit() {
        idCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().requestId()));
        createdCol.setCellValueFactory(c -> new SimpleStringProperty(formatInstant(c.getValue().createdAt())));
        byCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().createdBy()));

        TableColumnLayout.bindProportionalColumns(pendingTable, PENDING_COL_RATIOS, idCol, createdCol, byCol);
        TableColumnLayout.bindEllipsisCellFactory(idCol);
        TableColumnLayout.bindEllipsisCellFactory(createdCol);
        TableColumnLayout.bindEllipsisCellFactory(byCol);

        pendingTable.getSelectionModel().selectedItemProperty().addListener((obs, o, row) -> loadDetail(row));
        bindEmptyTable(pendingTable, "Không có yêu cầu Chờ xử lý — Sales có thể chưa gửi yêu cầu mới.");
        refresh();
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    @FXML
    private void onAccept() {
        ImportRequestDto selected = pendingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setScreenStatus("Chọn một yêu cầu trong bảng trước khi tiếp nhận.");
            return;
        }
        if (!UiTasks.confirm(
                "Tiếp nhận yêu cầu",
                "Tiếp nhận " + selected.requestId() + "?",
                "Trạng thái sẽ chuyển sang Đang xử lý. Bạn có thể gửi truy vấn tồn kho sau đó."
        )) {
            setScreenStatus("Đã hủy tiếp nhận.");
            return;
        }
        UiTasks.runWithStatus(
                "Đang tiếp nhận…",
                () -> {
                    ImportRequestDto accepted = app.acceptance().acceptRequest(selected.requestId());
                    setScreenStatus("Đã tiếp nhận " + accepted.requestId());
                    UiTasks.showInfo(
                            "Tiếp nhận thành công",
                            "Mã " + accepted.requestId() + " — " + StatusLabels.requestStatus(accepted.status())
                                    + "\nTiếp theo: menu Truy vấn tồn kho."
                    );
                    refresh();
                },
                "Sẵn sàng."
        );
    }

    private void refresh() {
        UiTasks.runWithStatus(
                "Đang tải…",
                () -> {
                    var items = app.acceptance().listPendingRequests();
                    pendingTable.setItems(FXCollections.observableArrayList(items));
                    setScreenStatus(items.isEmpty()
                            ? "Không có yêu cầu chờ."
                            : items.size() + " yêu cầu chờ tiếp nhận — chọn để xem chi tiết.");
                },
                "Danh sách đã cập nhật."
        );
    }

    private void loadDetail(ImportRequestDto summary) {
        if (summary == null) {
            detailArea.clear();
            return;
        }
        UiTasks.run(() -> {
            var full = app.acceptance().getRequest(summary.requestId()).orElse(summary);
            StringBuilder sb = new StringBuilder();
            sb.append("Mã: ").append(full.requestId()).append('\n');
            sb.append("Trạng thái: ").append(StatusLabels.requestStatus(full.status())).append('\n');
            sb.append("Người tạo: ").append(full.createdBy()).append('\n');
            if (full.items().isEmpty()) {
                sb.append("\n(Chọn lại dòng để tải danh sách mặt hàng.)");
            } else {
                full.items().forEach(item -> sb.append("  - ")
                        .append(item.merchandiseCode())
                        .append(" × ").append(item.quantityOrdered())
                        .append(' ').append(item.unit())
                        .append(" · ").append(item.desiredDeliveryDate())
                        .append('\n'));
            }
            detailArea.setText(sb.toString());
        });
    }

    private static String formatInstant(Instant instant) {
        return instant == null ? "—" : DT.format(instant);
    }
}
