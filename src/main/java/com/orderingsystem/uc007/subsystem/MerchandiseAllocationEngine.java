package com.orderingsystem.uc007.subsystem;

import com.orderingsystem.core.domain.DeliveryMeans;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Thuật toán phân bổ UC007
 * (1) ưu tiên tàu hơn hàng không → (2) ưu tiên Site có tồn kho lớn → (3) dùng ít Site nhất có thể.
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

    public static Plan allocate(int quantityNeeded, List<SitePool> shipPools, List<SitePool> airPools) {
        if (quantityNeeded <= 0) {
            return Plan.ok(List.of());
        }
        if (shipPools.isEmpty() && airPools.isEmpty()) {
            return Plan.withoutEligibleSite();
        }

        int totalShipCap = shipPools.stream().mapToInt(SitePool::stock).sum();
        int shipTarget = Math.min(quantityNeeded, totalShipCap);
        Map<String, Integer> shipBySite = shipTarget > 0
                ? allocateForMode(shipPools, shipTarget)
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
            airBySite = allocateForMode(airRemaining, remaining);
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

    static Map<String, Integer> allocateForMode(List<SitePool> pools, int target) {
        if (target <= 0 || pools.isEmpty()) {
            return Map.of();
        }

        List<SitePool> sorted = pools.stream()
                .sorted(Comparator.comparingInt(SitePool::stock).reversed()
                        .thenComparing(SitePool::siteCode))
                .toList();

        int n = sorted.size();
        List<Integer>[] bestStockRank = new List[1];
        int[] bestSiteCount = {Integer.MAX_VALUE};
        List<SitePool>[] bestChosen = new List[1];

        for (int siteCount = 1; siteCount <= n; siteCount++) {
            int k = siteCount;
            combine(sorted, k, 0, new ArrayList<>(), chosen -> {
                int capacity = chosen.stream().mapToInt(SitePool::stock).sum();
                if (capacity < target) {
                    return;
                }
                Map<String, Integer> allocation = distributeQuantities(chosen, target);
                int taken = allocation.values().stream().mapToInt(Integer::intValue).sum();
                if (taken < target) {
                    return;
                }
                List<Integer> stockRank = stockPriorityVector(chosen, allocation);
                int cmp = compareStockPriority(stockRank, bestStockRank[0]);
                if (cmp > 0 || (cmp == 0 && k < bestSiteCount[0])) {
                    bestStockRank[0] = stockRank;
                    bestSiteCount[0] = k;
                    bestChosen[0] = List.copyOf(chosen);
                }
            });
        }

        if (bestChosen[0] == null) {
            return Map.of();
        }
        return distributeQuantities(bestChosen[0], target);
    }

    private static List<Integer> stockPriorityVector(List<SitePool> chosen, Map<String, Integer> allocation) {
        return chosen.stream()
                .filter(p -> allocation.getOrDefault(p.siteCode(), 0) > 0)
                .sorted(Comparator.comparingInt(SitePool::stock).reversed()
                        .thenComparing(SitePool::siteCode))
                .map(SitePool::stock)
                .toList();
    }

    private static int compareStockPriority(List<Integer> candidate, List<Integer> best) {
        if (best == null) {
            return 1;
        }
        int len = Math.max(candidate.size(), best.size());
        for (int i = 0; i < len; i++) {
            int c = i < candidate.size() ? candidate.get(i) : 0;
            int b = i < best.size() ? best.get(i) : 0;
            if (c != b) {
                return Integer.compare(c, b);
            }
        }
        return 0;
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
