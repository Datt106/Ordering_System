package com.orderingsystem.uc007.support;

import com.orderingsystem.core.domain.ImportRequestItem;
import com.orderingsystem.core.domain.ItemStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Nhu cầu theo mã hàng sau khi gộp các dòng REQ trùng mã. */
public record MerchandiseDemand(
        String merchandiseCode,
        int quantityNeeded,
        LocalDate targetDate,
        String unit,
        List<Long> itemIds,
        boolean skippedNoSite
) {
    public static MerchandiseDemand from(ImportRequestItem item) {
        return new MerchandiseDemand(
                item.getMerchandiseCode(),
                item.getQuantityOrdered(),
                item.getDesiredDeliveryDate(),
                item.getUnit(),
                List.of(item.getId()),
                item.getItemStatus() == ItemStatus.KHONG_CO_SITE
        );
    }

    public MerchandiseDemand merge(ImportRequestItem item) {
        boolean noSite = skippedNoSite() || item.getItemStatus() == ItemStatus.KHONG_CO_SITE;
        LocalDate earliest = targetDate().isBefore(item.getDesiredDeliveryDate())
                ? targetDate()
                : item.getDesiredDeliveryDate();
        List<Long> ids = new ArrayList<>(itemIds());
        ids.add(item.getId());
        return new MerchandiseDemand(
                merchandiseCode(),
                quantityNeeded() + item.getQuantityOrdered(),
                earliest,
                unit(),
                ids,
                noSite
        );
    }
}
