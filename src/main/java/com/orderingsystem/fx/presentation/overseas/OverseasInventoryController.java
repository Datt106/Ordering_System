package com.orderingsystem.fx.presentation.overseas;

import com.orderingsystem.uc002.boundary.dto.ImportRequestDto;
import com.orderingsystem.uc006.boundary.dto.InventoryQueryDispatchResultDto;
import com.orderingsystem.fx.presentation.BaseViewController;
import com.orderingsystem.fx.presentation.UiTasks;
import com.orderingsystem.fx.presentation.ux.TableColumnLayout;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;

import java.util.List;

public class OverseasInventoryController extends BaseViewController {

    /** Mã YC 45% · Tiếp nhận bởi 55% */
    private static final double[] PROCESSING_COL_RATIOS = {0.45, 0.55};

    @FXML
    private TableView<ImportRequestDto> processingTable;
    @FXML
    private TableColumn<ImportRequestDto, String> idCol;
    @FXML
    private TableColumn<ImportRequestDto, String> byCol;
    @FXML
    private TextArea resultArea;
    @FXML
    private Button dispatchButton;
    @FXML
    private Button timeoutButton;

    @Override
    protected void onInit() {
        idCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().requestId()));
        byCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().processedBy() != null ? c.getValue().processedBy() : "—"));

        TableColumnLayout.bindProportionalColumns(processingTable, PROCESSING_COL_RATIOS, idCol, byCol);
        TableColumnLayout.bindEllipsisCellFactory(idCol);
        TableColumnLayout.bindEllipsisCellFactory(byCol);
        processingTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            boolean hasSelection = selected != null;
            setActionButtonsEnabled(hasSelection);
            if (hasSelection) {
                onStatus();
            }
        });
        setActionButtonsEnabled(false);
        bindTableScroll(processingTable);
        refresh();
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    @FXML
    private void onDispatch() {
        ImportRequestDto selected = requireSelection();
        if (selected == null) {
            return;
        }
        if (!UiTasks.confirm(
                "Gửi truy vấn tồn kho",
                "Gửi truy vấn cho " + selected.requestId() + "?",
                "Hệ thống sẽ gửi tới các Site đủ điều kiện (đã khai báo vận chuyển + mặt hàng KD)."
        )) {
            setScreenStatus("Đã hủy gửi truy vấn.");
            return;
        }
        String requestId = selected.requestId();
        UiTasks.runWithStatus(
                "Đang gửi truy vấn…",
                () -> app.uc006().dispatchInventoryQueries(requestId),
                this::showResult,
                "Truy vấn đã gửi."
        );
    }

    @FXML
    private void onStatus() {
        ImportRequestDto selected = requireSelection();
        if (selected == null) {
            return;
        }
        String requestId = selected.requestId();
        UiTasks.runWithStatus(
                "Đang tải trạng thái…",
                () -> app.uc006().getInventoryQueryStatus(requestId),
                this::showResult,
                "Trạng thái đã cập nhật."
        );
    }

    @FXML
    private void onTimeoutZero() {
        ImportRequestDto selected = requireSelection();
        if (selected == null) {
            return;
        }
        if (!UiTasks.confirm(
                "Ghi tồn kho = 0",
                "Ghi 0 cho mọi truy vấn Site chưa phản hồi?",
                "Chỉ dùng khi Site không trả lời trong thời hạn — thao tác khó hoàn tác."
        )) {
            setScreenStatus("Đã hủy.");
            return;
        }
        String requestId = selected.requestId();
        UiTasks.runWithStatus(
                "Đang xử lý timeout…",
                () -> {
                    int updated = app.uc006().applyTimeoutAsZeroStock(requestId);
                    InventoryQueryDispatchResultDto status = app.uc006().getInventoryQueryStatus(requestId);
                    return new TimeoutResult(updated, status);
                },
                result -> {
                    setScreenStatus("Đã ghi 0 tồn cho " + result.updatedCount() + " dòng chờ.");
                    showResult(result.status());
                },
                "Timeout đã áp dụng."
        );
    }

    private ImportRequestDto requireSelection() {
        ImportRequestDto selected = processingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setScreenStatus("Chọn một yêu cầu Đang xử lý trong bảng.");
        }
        return selected;
    }

    private void refresh() {
        UiTasks.runWithStatus(
                "Đang tải…",
                () -> app.uc005().listProcessingRequests(),
                this::applyProcessingList,
                "Danh sách sẵn sàng."
        );
    }

    private void applyProcessingList(List<ImportRequestDto> items) {
        processingTable.setItems(FXCollections.observableArrayList(items));
        setActionButtonsEnabled(processingTable.getSelectionModel().getSelectedItem() != null);
        setScreenStatus(items.isEmpty()
                ? "Chưa có yêu cầu đang xử lý."
                : items.size() + " yêu cầu — chọn rồi Gửi truy vấn để xem kết quả.");
    }

    private void showResult(InventoryQueryDispatchResultDto result) {
        setScreenStatus("Tổng " + result.totalQueries() + " truy vấn · còn chờ " + result.pendingQueries());
        StringBuilder sb = new StringBuilder();
        if (result.totalQueries() == 0) {
            sb.append("Chưa có truy vấn nào — bấm Gửi truy vấn hoặc kiểm tra Site/mặt hàng KD.\n");
        }
        result.siteGroups().forEach(g -> {
            sb.append("Site ").append(g.siteCode()).append(" — ").append(g.siteName()).append('\n');
            g.lines().forEach(line -> sb.append("  ")
                    .append(line.merchandiseCode())
                    .append(" | tồn=").append(line.inStockQuantity())
                    .append(line.pending() ? " (chờ Site)" : " (đã phản hồi)")
                    .append('\n'));
        });
        if (!result.merchandiseErrors().isEmpty()) {
            sb.append("\nMặt hàng không có Site phù hợp:\n");
            result.merchandiseErrors().forEach(e -> sb.append("  ").append(e.merchandiseCode())
                    .append(": ").append(e.message()).append('\n'));
        }
        resultArea.setText(sb.toString());
    }

    private record TimeoutResult(int updatedCount, InventoryQueryDispatchResultDto status) {
    }

    private void setActionButtonsEnabled(boolean enabled) {
        dispatchButton.setDisable(!enabled);
        timeoutButton.setDisable(!enabled);
    }
}
