package com.orderingsystem.uc007.support;

import com.orderingsystem.core.domain.ImportRequestItem;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DemandAggregator {

    private DemandAggregator() {
    }

    public static Map<String, MerchandiseDemand> aggregate(List<ImportRequestItem> items) {
        Map<String, MerchandiseDemand> map = new LinkedHashMap<>();
        for (ImportRequestItem item : items) {
            String code = item.getMerchandiseCode();
            MerchandiseDemand existing = map.get(code);
            if (existing == null) {
                map.put(code, MerchandiseDemand.from(item));
            } else {
                map.put(code, existing.merge(item));
            }
        }
        return map;
    }
}
