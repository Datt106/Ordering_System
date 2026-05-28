package com.orderingsystem.uc007.controller;

import com.orderingsystem.core.domain.DeliveryMeans;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Thuật toán phân bổ UC007 — lexicographic: tàu → ít Site → tồn lớn (tie-break).
 */
public final class MerchandiseAllocationEngine {

    private MerchandiseAllocationEngine() {
    }

    public record SitePool(String siteCode, int stock) {
    }

    public record AllocationLine(String siteCode, DeliveryMeans deliveryMeans, int quantity) {
    }

    public record Plan(List<AllocationLine> lines, int shortfall, boolean hasNoEligibleSite) {

        static Plan ok(List<AllocationLine> lines) {
            return new Plan(lines, 0, false);
        }

        static Plan shortfall(int missing) {
            return new Plan(List.of(), missing, false);
        }

        static Plan withoutEligibleSite() {
            return new Plan(List.of(), 0, true);
        }
    }

    /**
     * @param quantityNeeded Q
     * @param shipPools      Site đáp ứng ETA tàu (tồn &gt; 0)
     * @param airPools       Site đáp ứng ETA bay (tồn &gt; 0, có thể trùng mã với ship)
     */
    static Plan allocate(int quantityNeeded, List<SitePool> shipPools, List<SitePool> airPools) {
        if (quantityNeeded <= 0) {
            return Plan.ok(List.of());
        }
        if (shipPools.isEmpty() && airPools.isEmpty()) {
            return Plan.withoutEligibleSite();
        }

        int totalShipCap = shipPools.stream().mapToInt(SitePool::stock).sum();
        int shipTarget = Math.min(quantityNeeded, totalShipCap);
        Map<String, Integer> shipBySite = shipTarget > 0
                ? allocateWithMinSites(shipPools, shipTarget)
                : Map.of();

        int shipTaken = shipBySite.values().stream().mapToInt(Integer::intValue).sum();
        int remaining = quantityNeeded - shipTaken;

        Map<String, Integer> airBySite = Map.of();
        if (remaining > 0) {
            List<SitePool> airRemaining = new ArrayList<>();
            for (SitePool air : airPools) {
                int left = air.stock() - shipBySite.getOrDefault(air.siteCode(), 0);
                if (left > 0) {
                    airRemaining.add(new SitePool(air.siteCode(), left));
                }
            }
            if (airRemaining.isEmpty()) {
                return Plan.shortfall(remaining);
            }
            airBySite = allocateWithMinSites(airRemaining, remaining);
            int airTaken = airBySite.values().stream().mapToInt(Integer::intValue).sum();
            if (airTaken < remaining) {
                return Plan.shortfall(remaining - airTaken);
            }
        }

        List<AllocationLine> lines = new ArrayList<>();
        for (var entry : shipBySite.entrySet()) {
            if (entry.getValue() > 0) {
                lines.add(new AllocationLine(entry.getKey(), DeliveryMeans.SHIP_DELIVERY, entry.getValue()));
            }
        }
        for (var entry : airBySite.entrySet()) {
            if (entry.getValue() > 0) {
                lines.add(new AllocationLine(entry.getKey(), DeliveryMeans.AIR_DELIVERY, entry.getValue()));
            }
        }
        return Plan.ok(lines);
    }

    /** Chọn ít Site nhất để đạt {@code target}, phân bổ ưu tiên Site tồn lớn. */
    static Map<String, Integer> allocateWithMinSites(List<SitePool> pools, int target) {
        if (target <= 0 || pools.isEmpty()) {
            return Map.of();
        }

        List<SitePool> sorted = pools.stream()
                .sorted(Comparator.comparingInt(SitePool::stock).reversed()
                        .thenComparing(SitePool::siteCode))
                .toList();

        int n = sorted.size();
        int maxSum = sorted.stream().mapToInt(SitePool::stock).sum();
        int cap = Math.min(target, maxSum);

        int minSiteCount = minSiteCountToReach(sorted, cap);
        if (minSiteCount == Integer.MAX_VALUE) {
            return Map.of();
        }

        List<SitePool> chosen = selectSitesWithCount(sorted, minSiteCount, cap);
        return distributeQuantities(chosen, cap);
    }

    private static int minSiteCountToReach(List<SitePool> sortedByStockDesc, int target) {
        int n = sortedByStockDesc.size();
        boolean[][] reachable = new boolean[target + 1][n + 1];
        reachable[0][0] = true;

        for (SitePool pool : sortedByStockDesc) {
            int siteCap = Math.min(target, pool.stock());
            for (int sum = target; sum >= 0; sum--) {
                for (int count = n; count >= 0; count--) {
                    if (!reachable[sum][count]) {
                        continue;
                    }
                    int nextSum = Math.min(target, sum + siteCap);
                    reachable[nextSum][count + 1] = true;
                }
            }
        }

        for (int count = 1; count <= n; count++) {
            if (reachable[target][count]) {
                return count;
            }
        }
        return Integer.MAX_VALUE;
    }

    /** Tập Site cỡ {@code siteCount} đạt tổng tồn ≥ target; tie-break tổng tồn lớn nhất. */
    private static List<SitePool> selectSitesWithCount(List<SitePool> sorted, int siteCount, int target) {
        List<SitePool>[] bestHolder = new List[1];
        int[] bestStockSum = {-1};
        combine(sorted, siteCount, 0, new ArrayList<>(), chosen -> {
            int sum = chosen.stream().mapToInt(SitePool::stock).sum();
            if (sum >= target && sum > bestStockSum[0]) {
                bestStockSum[0] = sum;
                bestHolder[0] = List.copyOf(chosen);
            }
        });
        if (bestHolder[0] != null) {
            return bestHolder[0];
        }
        return sorted.subList(0, Math.min(siteCount, sorted.size()));
    }

    private static void combine(
            List<SitePool> pools,
            int k,
            int start,
            List<SitePool> current,
            java.util.function.Consumer<List<SitePool>> onComplete
    ) {
        if (current.size() == k) {
            onComplete.accept(current);
            return;
        }
        for (int i = start; i < pools.size(); i++) {
            current.add(pools.get(i));
            combine(pools, k, i + 1, current, onComplete);
            current.removeLast();
        }
    }

    private static Map<String, Integer> distributeQuantities(List<SitePool> chosen, int target) {
        List<SitePool> order = chosen.stream()
                .sorted(Comparator.comparingInt(SitePool::stock).reversed()
                        .thenComparing(SitePool::siteCode))
                .toList();
        Map<String, Integer> result = new LinkedHashMap<>();
        int remaining = target;
        for (SitePool pool : order) {
            if (remaining <= 0) {
                break;
            }
            int take = Math.min(pool.stock(), remaining);
            if (take > 0) {
                result.put(pool.siteCode(), take);
                remaining -= take;
            }
        }
        return result;
    }
}
