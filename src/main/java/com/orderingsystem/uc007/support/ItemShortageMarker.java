package com.orderingsystem.uc007.support;

import com.orderingsystem.core.domain.ItemStatus;
import com.orderingsystem.infrastructure.database.ImportRequestRepository;

public class ItemShortageMarker {

    private final ImportRequestRepository importRequestRepository;

    public ItemShortageMarker(ImportRequestRepository importRequestRepository) {
        this.importRequestRepository = importRequestRepository;
    }

    public void markIfConfirming(MerchandiseDemand demand, boolean forConfirm) {
        if (!forConfirm) {
            return;
        }
        for (Long itemId : demand.itemIds()) {
            importRequestRepository.updateItemStatus(itemId, ItemStatus.LOI_KHONG_DU_HANG);
        }
    }
}
