import { useMemo, useState } from "react";
import { Eye, MoreHorizontal, Search, Filter } from "lucide-react";
import StatusBadge from "./StatusBadge";
import EmptyState from "./EmptyState";
import { IMPORT_REQUESTS, STATUS_CONFIG } from "../data/mockData";

/**
 * Bảng yêu cầu nhập hàng — cột: ID, Người tạo, Mặt hàng, Số dòng, Trạng thái, Hành động.
 */
export default function ImportRequestsTable() {
  const [query, setQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");

  const filtered = useMemo(() => {
    return IMPORT_REQUESTS.filter((row) => {
      const matchStatus =
        statusFilter === "ALL" || row.status === statusFilter;
      const q = query.trim().toLowerCase();
      const matchQuery =
        !q ||
        row.id.toLowerCase().includes(q) ||
        row.createdBy.toLowerCase().includes(q) ||
        row.merchandiseSummary.toLowerCase().includes(q);
      return matchStatus && matchQuery;
    });
  }, [query, statusFilter]);

  return (
    <section className="card overflow-hidden p-0">
      {/* Toolbar */}
      <div className="flex flex-col gap-4 border-b border-gray-200 p-6 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 className="text-lg font-bold text-ink">
            Yêu cầu nhập hàng
          </h2>
          <p className="mt-1 text-sm text-ink-secondary">
            Theo dõi tiến độ từ Sales đến tách đơn Site
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          <div className="relative min-w-[200px] flex-1 sm:flex-none">
            <Search
              className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400"
              aria-hidden
            />
            <input
              type="search"
              placeholder="Tìm mã, người tạo…"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              className="input-field w-full pl-10 sm:w-56"
              aria-label="Tìm yêu cầu"
            />
          </div>
          <div className="relative">
            <Filter
              className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400"
              aria-hidden
            />
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="input-field appearance-none pl-10 pr-8 font-medium"
              aria-label="Lọc trạng thái"
            >
              <option value="ALL">Tất cả trạng thái</option>
              {Object.entries(STATUS_CONFIG).map(([key, cfg]) => (
                <option key={key} value={key}>
                  {cfg.label}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {filtered.length === 0 ? (
        <EmptyState
          title="Không tìm thấy yêu cầu"
          description="Không có bản ghi khớp từ khóa hoặc bộ lọc. Thử xóa bộ lọc để xem toàn bộ."
          actionLabel="Xóa bộ lọc"
          onAction={() => {
            setQuery("");
            setStatusFilter("ALL");
          }}
        />
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full min-w-[720px] text-left text-sm">
            <thead>
              <tr className="border-b border-gray-200 bg-gray-100">
                <th className="px-6 py-3.5 font-bold text-ink-secondary">
                  Mã yêu cầu
                </th>
                <th className="px-4 py-3.5 font-bold text-ink-secondary">
                  Người tạo
                </th>
                <th className="px-4 py-3.5 font-bold text-ink-secondary">
                  Mặt hàng
                </th>
                <th className="px-4 py-3.5 font-bold text-ink-secondary">
                  Số dòng
                </th>
                <th className="px-4 py-3.5 font-bold text-ink-secondary">
                  Trạng thái
                </th>
                <th className="px-4 py-3.5 font-bold text-ink-secondary">
                  Ngày tạo
                </th>
                <th className="px-6 py-3.5 text-right font-bold text-ink-secondary">
                  Hành động
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {filtered.map((row, index) => {
                const status = STATUS_CONFIG[row.status];
                return (
                  <tr
                    key={row.id}
                    className={`transition-colors duration-150 hover:bg-accent-muted ${
                      index % 2 === 1 ? "bg-surface-muted" : "bg-surface"
                    }`}
                  >
                    <td className="px-6 py-4 font-mono text-xs font-semibold text-ink">
                      {row.id}
                    </td>
                    <td className="px-4 py-4">
                      <span className="font-medium text-ink">
                        {row.createdBy}
                      </span>
                      <span className="mt-0.5 block text-xs text-ink-muted">
                        {row.department}
                      </span>
                    </td>
                    <td className="max-w-[180px] truncate px-4 py-4 text-ink-secondary">
                      {row.merchandiseSummary}
                    </td>
                    <td className="px-4 py-4 text-ink-secondary">{row.itemCount}</td>
                    <td className="px-4 py-4">
                      <StatusBadge label={status.label} tone={status.tone} />
                    </td>
                    <td className="whitespace-nowrap px-4 py-4 text-ink-muted">
                      {row.createdAt}
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center justify-end gap-1">
                        <button
                          type="button"
                          className="btn-ghost rounded-lg p-2"
                          title="Xem chi tiết"
                          aria-label={`Xem ${row.id}`}
                        >
                          <Eye className="h-4 w-4" />
                        </button>
                        <button
                          type="button"
                          className="btn-ghost rounded-lg p-2"
                          title="Tùy chọn"
                          aria-label="Thêm thao tác"
                        >
                          <MoreHorizontal className="h-4 w-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {filtered.length > 0 && (
        <footer className="border-t border-gray-200 px-6 py-3 text-xs text-ink-muted">
          Hiển thị {filtered.length} / {IMPORT_REQUESTS.length} yêu cầu
        </footer>
      )}
    </section>
  );
}
