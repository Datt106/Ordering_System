package com.orderingsystem.fx.presentation.ux;

import com.orderingsystem.uc001.boundary.dto.StandardMerchandiseDto;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;

import java.util.List;
import java.util.Optional;

/**
 * ComboBox chọn mã hàng kèm tên từ danh mục chuẩn.
 */
public final class MerchandisePicker {

    private MerchandisePicker() {
    }

    public static void bindCatalog(ComboBox<String> combo, List<StandardMerchandiseDto> catalog) {
        combo.setEditable(true);
        combo.setItems(FXCollections.observableArrayList(
                catalog.stream().map(MerchandisePicker::formatOption).toList()));
        if (!catalog.isEmpty()) {
            combo.getSelectionModel().selectFirst();
        }
    }

    public static String formatOption(StandardMerchandiseDto item) {
        String name = item.merchandiseName() != null ? item.merchandiseName() : "";
        return item.merchandiseCode() + " — " + name;
    }

    public static String extractCode(String selection) {
        if (selection == null || selection.isBlank()) {
            return "";
        }
        int sep = selection.indexOf(" — ");
        return (sep > 0 ? selection.substring(0, sep) : selection).trim();
    }

    public static Optional<StandardMerchandiseDto> findBySelection(
            String selection,
            List<StandardMerchandiseDto> catalog
    ) {
        String code = extractCode(selection);
        if (code.isBlank()) {
            return Optional.empty();
        }
        return catalog.stream()
                .filter(item -> item.merchandiseCode().equalsIgnoreCase(code))
                .findFirst();
    }
}
