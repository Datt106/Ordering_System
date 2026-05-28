package com.orderingsystem.core.domain;

/** Giá trị lưu DB; hiển thị/ghi file theo SRS: "ship delivery" / "air delivery". */
public enum DeliveryMeans {
    SHIP_DELIVERY,
    AIR_DELIVERY;

    public String toExternalValue() {
        return switch (this) {
            case SHIP_DELIVERY -> "ship delivery";
            case AIR_DELIVERY -> "air delivery";
        };
    }
}
