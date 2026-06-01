package com.orderingsystem.core;

import com.orderingsystem.auth.Session;
import com.orderingsystem.core.domain.ImportRequest;
import com.orderingsystem.core.domain.ItemStatus;
import com.orderingsystem.core.domain.RequestStatus;
import com.orderingsystem.infrastructure.database.ImportRequestRepository;
import com.orderingsystem.uc007.boundary.dto.MerchandiseSplitPlanDto;

import java.util.List;

/**
 * Cập nhật {@link RequestStatus#LOI} khi yêu cầu không thể đáp ứng (UC003 / usecase).
 */
public final class RequestStatusEvaluator {

    private RequestStatusEvaluator() {
    }

    /** Sau truy vấn tồn kho: mọi dòng mặt hàng đều không có Site phù hợp. */
    public static void markErrorIfAllItemsUnfulfillable(ImportRequestRepository repository, String requestId) {
        ImportRequest request = repository.findByIdWithItems(requestId).orElse(null);
        if (request == null || request.getStatus() != RequestStatus.DANG_XU_LY || request.getItems().isEmpty()) {
            return;
        }
        boolean allFailed = request.getItems().stream()
                .allMatch(item -> item.getItemStatus() != ItemStatus.OK);
        if (allFailed) {
            repository.updateStatus(requestId, RequestStatus.LOI, actorUsername());
        }
    }

    /** Sau xem trước tách đơn: đã có tồn kho nhưng không mã hàng nào phân bổ được. */
    public static void markErrorIfNoMerchandiseCanBeSplit(
            ImportRequestRepository repository,
            String requestId,
            boolean inventoryReady,
            List<MerchandiseSplitPlanDto> plans
    ) {
        if (!inventoryReady || plans.isEmpty() || plans.stream().anyMatch(MerchandiseSplitPlanDto::success)) {
            return;
        }
        ImportRequest request = repository.findById(requestId).orElse(null);
        if (request == null || request.getStatus() != RequestStatus.DANG_XU_LY) {
            return;
        }
        repository.updateStatus(requestId, RequestStatus.LOI, actorUsername());
    }

    /** Gửi lại truy vấn / xử lý lại — chuyển từ Lỗi về Đang xử lý. */
    public static void clearErrorStatusIfProcessing(ImportRequestRepository repository, String requestId) {
        ImportRequest request = repository.findById(requestId).orElse(null);
        if (request != null && request.getStatus() == RequestStatus.LOI) {
            repository.updateStatus(requestId, RequestStatus.DANG_XU_LY, actorUsername());
        }
    }

    private static String actorUsername() {
        return Session.getCurrentUser()
                .map(user -> user.username())
                .orElse("system");
    }
}
