package com.orderingsystem.fx.presentation.overseas;

import com.orderingsystem.core.domain.DeliveryMeans;
import com.orderingsystem.uc007.boundary.dto.OrderSplitLineDto;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/** Dòng phương án tách đơn — bind trực tiếp lên bảng chỉnh tay. */
public final class EditableSplitLineRow {

    private final StringProperty siteCode = new SimpleStringProperty();
    private final StringProperty merchandiseCode = new SimpleStringProperty();
    private final IntegerProperty quantity = new SimpleIntegerProperty();
    private final ObjectProperty<DeliveryMeans> deliveryMeans =
            new SimpleObjectProperty<>(DeliveryMeans.SHIP_DELIVERY);

    public static EditableSplitLineRow from(OrderSplitLineDto line) {
        EditableSplitLineRow row = new EditableSplitLineRow();
        row.siteCode.set(line.siteCode());
        row.merchandiseCode.set(line.merchandiseCode());
        row.quantity.set(line.quantity());
        row.deliveryMeans.set(line.deliveryMeans());
        return row;
    }

    public StringProperty siteCodeProperty() {
        return siteCode;
    }

    public StringProperty merchandiseCodeProperty() {
        return merchandiseCode;
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    public ObjectProperty<DeliveryMeans> deliveryMeansProperty() {
        return deliveryMeans;
    }

    public String getSiteCode() {
        return siteCode.get();
    }

    public String getMerchandiseCode() {
        return merchandiseCode.get();
    }

    public int getQuantity() {
        return quantity.get();
    }

    public DeliveryMeans getDeliveryMeans() {
        return deliveryMeans.get();
    }
}
