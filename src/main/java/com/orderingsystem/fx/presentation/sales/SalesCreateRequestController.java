package com.orderingsystem.fx.presentation.sales;

import com.orderingsystem.uc001.boundary.dto.StandardMerchandiseDto;
import com.orderingsystem.uc002.boundary.dto.CreateImportRequestLineInput;
import com.orderingsystem.fx.presentation.BaseViewController;
import com.orderingsystem.fx.presentation.UiTasks;
import com.orderingsystem.fx.presentation.ux.FormPanels;
import com.orderingsystem.fx.presentation.ux.FormValidation;
import com.orderingsystem.fx.presentation.ux.MerchandisePicker;
import com.orderingsystem.fx.presentation.ux.SpinnerInputs;
import com.orderingsystem.fx.presentation.ux.TableColumnLayout;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SalesCreateRequestController extends BaseViewController {

    private static final double[] LINE_COL_RATIOS = {0.25, 0.15, 0.15, 0.45};
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private ComboBox<String> codeCombo;
    @FXML
    private Spinner<Integer> qtySpinner;
    @FXML
    private TextField unitField;
    @FXML
    private DatePicker deliveryPicker;
    @FXML
    private VBox lineFormPanel;
    @FXML
    private VBox tableContainer;
    @FXML
    private TableView<LineRow> linesTable;
    @FXML
    private TableColumn<LineRow, String> codeCol;
    @FXML
    private TableColumn<LineRow, String> qtyCol;
    @FXML
    private TableColumn<LineRow, String> unitCol;
    @FXML
    private TableColumn<LineRow, String> dateCol;
    @FXML
    private Label resultLabel;
    @FXML
    private Button submitButton;

    private final List<LineRow> lines = new ArrayList<>();
    private List<StandardMerchandiseDto> catalog = List.of();

    @Override
    protected void onInit() {
        SpinnerInputs.configureIntegerSpinner(qtySpinner, 1, 999_999, 1);
        codeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().code()));
        qtyCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().qty())));
        unitCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().unit()));
        dateCol.setCellValueFactory(c -> new SimpleStringProperty(formatDate(c.getValue().deliveryDate())));

        TableColumnLayout.bindProportionalColumns(linesTable, LINE_COL_RATIOS, codeCol, qtyCol, unitCol, dateCol);
        TableColumnLayout.bindEllipsisCellFactory(codeCol);
        TableColumnLayout.bindEllipsisCellFactory(unitCol);
        TableColumnLayout.bindEllipsisCellFactory(dateCol);
        bindTableScroll(linesTable, tableContainer);

        deliveryPicker.setValue(LocalDate.now().plusDays(7));
        unitField.setText("pcs");
        updateSubmitState();
        loadCatalog();
        refreshTable();
    }

    @FXML
    private void onShowLineForm() {
        deliveryPicker.setValue(LocalDate.now().plusDays(7));
        if (!catalog.isEmpty() && codeCombo.getValue() == null) {
            codeCombo.getSelectionModel().selectFirst();
        }
        FormPanels.open(lineFormPanel);
    }

    @FXML
    private void onLineFormOk() {
        LocalDate date = deliveryPicker.getValue();
        String code = MerchandisePicker.extractCode(codeCombo.getEditor().getText());
        if (code.isBlank()) {
            code = MerchandisePicker.extractCode(codeCombo.getValue());
        }
        try {
            if (code.isBlank()) {
                throw new IllegalArgumentException("Chọn hoặc nhập mã hàng từ danh mục.");
            }
            FormValidation.requireNonBlank(unitField, "Nhập đơn vị.", () -> {});
            if (date == null) {
                throw new IllegalArgumentException("Chọn ngày nhận mong muốn.");
            }
            if (!date.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Ngày nhận mong muốn phải sau ngày hiện tại.");
            }
        } catch (IllegalArgumentException ex) {
            UiTasks.showError(ex);
            return;
        }
        lines.add(new LineRow(code, qtySpinner.getValue(), unitField.getText().trim(), date));
        refreshTable();
        FormPanels.close(lineFormPanel);
        resultLabel.setText("");
    }

    @FXML
    private void onLineFormClose() {
        FormPanels.close(lineFormPanel);
    }

    @FXML
    private void onRemoveLine() {
        LineRow selected = linesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiTasks.showError(new IllegalArgumentException("Chọn dòng cần xóa trong bảng."));
            return;
        }
        lines.remove(selected);
        refreshTable();
    }

    @FXML
    private void onClearAll() {
        if (lines.isEmpty()) {
            return;
        }
        if (!UiTasks.confirm("Xóa tất cả dòng", "Xóa toàn bộ dòng đã nhập?", "Bạn sẽ phải nhập lại từ đầu.")) {
            return;
        }
        lines.clear();
        refreshTable();
        resultLabel.setText("");
    }

    @FXML
    private void onSubmit() {
        if (lines.isEmpty()) {
            UiTasks.showError(new IllegalArgumentException("Thêm ít nhất một dòng trước khi gửi."));
            return;
        }
        if (!UiTasks.confirm(
                "Gửi yêu cầu",
                "Gửi yêu cầu nhập hàng với " + lines.size() + " dòng?",
                "Yêu cầu sẽ chuyển sang trạng thái Chờ xử lý cho bộ phận Đặt hàng quốc tế."
        )) {
            return;
        }
        List<CreateImportRequestLineInput> inputs = lines.stream()
                .map(r -> new CreateImportRequestLineInput(r.code(), r.qty(), r.unit(), r.deliveryDate()))
                .toList();
        UiTasks.runWithStatus(
                "Đang gửi yêu cầu…",
                () -> app.uc002().createImportRequest(inputs),
                created -> {
                    lines.clear();
                    refreshTable();
                    resultLabel.setText("Mã yêu cầu: " + created.requestId());
                    UiTasks.showInfo(
                            "Yêu cầu đã gửi",
                            "Mã: " + created.requestId() + "\nTheo dõi tiến độ tại menu Theo dõi yêu cầu."
                    );
                },
                "Sẵn sàng tạo yêu cầu mới."
        );
    }

    private void loadCatalog() {
        UiTasks.runWithStatus(
                "Đang tải danh mục…",
                () -> app.uc001().listAll(),
                items -> {
                    catalog = items;
                    MerchandisePicker.bindCatalog(codeCombo, catalog);
                },
                "Danh mục sẵn sàng."
        );
    }

    private void refreshTable() {
        linesTable.setItems(FXCollections.observableArrayList(lines));
        updateSubmitState();
    }

    private void updateSubmitState() {
        submitButton.setDisable(lines.isEmpty());
    }

    private static String formatDate(LocalDate date) {
        return date == null ? "—" : DATE_FMT.format(date);
    }

    public record LineRow(String code, int qty, String unit, LocalDate deliveryDate) {
    }
}
