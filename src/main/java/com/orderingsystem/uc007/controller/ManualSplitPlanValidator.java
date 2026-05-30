package com.orderingsystem.uc007.controller;

import com.orderingsystem.core.domain.DeliveryMeans;
import com.orderingsystem.core.domain.Site;
import com.orderingsystem.uc007.boundary.dto.ManualSplitLineInput;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Kiểm tra phương án phân bổ do người dùng chỉnh tay (FR-06.9). */
final class ManualSplitPlanValidator {

    record StockSnapshot(int quantity, String unit, boolean responded) {
    }

    record DemandSnapshot(
            String merchandiseCode,
            int quantityNeeded,
            LocalDate targetDate,
            String unit,
            boolean skippedNoSite
    ) {
    }

    private ManualSplitPlanValidator() {
    }

    static List<String> validate(
            String requestId,
            LocalDate startDate,
            boolean inventoryReady,
            Map<String, DemandSnapshot> demands,
            Map<String, Site> sitesByCode,
            Map<String, Map<String, StockSnapshot>> stockBySiteMerchandise,
            List<ManualSplitLineInput> lines
    ) {
        List<String> errors = new ArrayList<>();

        if (!inventoryReady) {
            errors.add("Chưa đủ phản hồi tồn kho từ Site — không thể xác nhận phương án.");
            return errors;
        }
        if (lines == null || lines.isEmpty()) {
            errors.add("Phương án trống — cần ít nhất một dòng phân bổ.");
            return errors;
        }

        Map<String, List<ManualSplitLineInput>> byMerchandise = new LinkedHashMap<>();
        for (int i = 0; i < lines.size(); i++) {
            ManualSplitLineInput line = lines.get(i);
            int row = i + 1;
            String siteCode = normalize(line.siteCode());
            String merchandiseCode = normalize(line.merchandiseCode());

            if (siteCode == null) {
                errors.add("Dòng " + row + ": thiếu mã Site.");
                continue;
            }
            if (merchandiseCode == null) {
                errors.add("Dòng " + row + ": thiếu mã hàng.");
                continue;
            }
            if (line.quantity() <= 0) {
                errors.add("Dòng " + row + ": số lượng phải lớn hơn 0.");
            }
            if (line.deliveryMeans() == null) {
                errors.add("Dòng " + row + ": chọn phương tiện vận chuyển.");
            }

            DemandSnapshot demand = demands.get(merchandiseCode);
            if (demand == null) {
                errors.add("Dòng " + row + ": mã hàng " + merchandiseCode + " không thuộc yêu cầu " + requestId + ".");
            } else if (demand.skippedNoSite()) {
                errors.add("Dòng " + row + ": mặt hàng " + merchandiseCode + " không có Site kinh doanh.");
            }

            Site site = sitesByCode.get(siteCode);
            if (site == null) {
                errors.add("Dòng " + row + ": Site " + siteCode + " không tồn tại.");
            } else if (site.getShipDays() == null || site.getAirDays() == null) {
                errors.add("Dòng " + row + ": Site " + siteCode + " chưa khai báo vận chuyển.");
            }

            StockSnapshot stock = stockBySiteMerchandise
                    .getOrDefault(siteCode, Map.of())
                    .get(merchandiseCode);
            if (stock == null || !stock.responded()) {
                errors.add("Dòng " + row + ": chưa có phản hồi tồn kho cho " + siteCode + " / " + merchandiseCode + ".");
            }

            if (line.deliveryMeans() != null && demand != null && site != null
                    && site.getShipDays() != null && site.getAirDays() != null) {
                LocalDate eta = line.deliveryMeans() == DeliveryMeans.SHIP_DELIVERY
                        ? startDate.plusDays(site.getShipDays())
                        : startDate.plusDays(site.getAirDays());
                if (eta.isAfter(demand.targetDate())) {
                    errors.add("Dòng " + row + ": " + siteCode + " không kịp ngày nhận mong muốn ("
                            + demand.targetDate() + ") với phương tiện đã chọn.");
                }
            }

            if (siteCode != null && merchandiseCode != null) {
                byMerchandise.computeIfAbsent(merchandiseCode, k -> new ArrayList<>()).add(line);
            }
        }

        if (!errors.isEmpty()) {
            return errors;
        }

        Map<String, Map<String, Integer>> usedBySiteMerchandise = new HashMap<>();
        for (Map.Entry<String, List<ManualSplitLineInput>> entry : byMerchandise.entrySet()) {
            String merchandiseCode = entry.getKey();
            DemandSnapshot demand = demands.get(merchandiseCode);
            int total = entry.getValue().stream().mapToInt(ManualSplitLineInput::quantity).sum();
            if (total != demand.quantityNeeded()) {
                errors.add("Mặt hàng " + merchandiseCode + ": tổng phân bổ " + total
                        + " khác số lượng yêu cầu " + demand.quantityNeeded() + ".");
            }

            for (ManualSplitLineInput line : entry.getValue()) {
                String siteCode = normalize(line.siteCode());
                int used = usedBySiteMerchandise
                        .computeIfAbsent(siteCode, k -> new HashMap<>())
                        .merge(merchandiseCode, line.quantity(), Integer::sum);
                StockSnapshot stock = stockBySiteMerchandise.get(siteCode).get(merchandiseCode);
                if (used > stock.quantity()) {
                    errors.add("Site " + siteCode + " / " + merchandiseCode
                            + ": tổng lấy " + used + " vượt tồn kho " + stock.quantity() + ".");
                }
            }
        }

        for (DemandSnapshot demand : demands.values()) {
            if (demand.skippedNoSite()) {
                if (byMerchandise.containsKey(demand.merchandiseCode())) {
                    errors.add("Mặt hàng " + demand.merchandiseCode() + " không có Site kinh doanh — không được phân bổ.");
                }
                continue;
            }
            if (!byMerchandise.containsKey(demand.merchandiseCode())) {
                errors.add("Thiếu phân bổ cho mặt hàng " + demand.merchandiseCode() + ".");
            }
        }

        return errors;
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
