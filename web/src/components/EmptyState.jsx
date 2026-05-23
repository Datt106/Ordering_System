import { Inbox } from "lucide-react";

/**
 * Empty state — khi không có dữ liệu (UX heuristic #8).
 */
export default function EmptyState({
  title = "Chưa có dữ liệu",
  description = "Thử đổi bộ lọc hoặc tạo mục mới.",
  actionLabel,
  onAction,
}) {
  return (
    <div className="flex flex-col items-center justify-center px-6 py-16 text-center">
      <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-gray-100 text-ink-muted">
        <Inbox className="h-8 w-8" aria-hidden />
      </div>
      <h3 className="text-base font-semibold text-ink">{title}</h3>
      <p className="mt-2 max-w-sm text-sm leading-relaxed text-ink-secondary">
        {description}
      </p>
      {actionLabel && onAction && (
        <button type="button" onClick={onAction} className="btn-primary mt-6">
          {actionLabel}
        </button>
      )}
    </div>
  );
}
