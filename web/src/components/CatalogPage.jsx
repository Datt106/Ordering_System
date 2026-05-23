import { useMemo, useState } from "react";
import { RefreshCw } from "lucide-react";
import { CATALOG_ITEMS } from "../data/mockData";
import EmptyState from "./EmptyState";

/**
 * Danh mục mặt hàng chuẩn — bảng full width, table-layout: fixed, 15% / 35% / 50%.
 * Theme tối giữ nguyên theo image_0 (chỉ layout bảng thay đổi).
 */
export default function CatalogPage() {
  const [items, setItems] = useState(CATALOG_ITEMS);
  const [code, setCode] = useState("");
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [selectedCode, setSelectedCode] = useState(null);
  const [status, setStatus] = useState("Chọn dòng trong bảng hoặc nhập mã mới.");

  const isFormValid = code.trim() && name.trim();

  const sortedItems = useMemo(
    () => [...items].sort((a, b) => a.merchandiseCode.localeCompare(b.merchandiseCode)),
    [items]
  );

  const selectRow = (row) => {
    setSelectedCode(row.merchandiseCode);
    setCode(row.merchandiseCode);
    setName(row.merchandiseName);
    setDescription(row.description ?? "");
    setStatus(`Đang sửa: ${row.merchandiseCode}`);
  };

  const clearForm = () => {
    setSelectedCode(null);
    setCode("");
    setName("");
    setDescription("");
  };

  const onRefresh = () => {
    setItems(CATALOG_ITEMS);
    setStatus(`Hiển thị ${CATALOG_ITEMS.length} mặt hàng.`);
  };

  const onAdd = () => {
    if (!isFormValid) {
      setStatus("Nhập đủ Mã và Tên trước khi Thêm.");
      return;
    }
    const c = code.trim();
    if (items.some((i) => i.merchandiseCode === c)) {
      setStatus(`Mã ${c} đã tồn tại.`);
      return;
    }
    setItems((prev) => [
      ...prev,
      {
        merchandiseCode: c,
        merchandiseName: name.trim(),
        description: description.trim() || null,
      },
    ]);
    setStatus(`Đã thêm: ${c}`);
  };

  const onUpdate = () => {
    if (!isFormValid) {
      setStatus("Nhập đủ Mã và Tên trước khi Cập nhật.");
      return;
    }
    const c = code.trim();
    if (!items.some((i) => i.merchandiseCode === c)) {
      setStatus(`Mã ${c} không tồn tại.`);
      return;
    }
    setItems((prev) =>
      prev.map((i) =>
        i.merchandiseCode === c
          ? {
              merchandiseCode: c,
              merchandiseName: name.trim(),
              description: description.trim() || null,
            }
          : i
      )
    );
    setStatus(`Đã cập nhật: ${c}`);
  };

  const onDelete = () => {
    const c = code.trim();
    if (!c || !items.some((i) => i.merchandiseCode === c)) {
      setStatus("Chọn mặt hàng cần xóa trong bảng hoặc nhập mã.");
      return;
    }
    if (!window.confirm(`Bạn có chắc muốn xóa mặt hàng ${c}?`)) {
      setStatus("Đã hủy xóa.");
      return;
    }
    setItems((prev) => prev.filter((i) => i.merchandiseCode !== c));
    clearForm();
    setStatus(`Đã xóa: ${c}`);
  };

  return (
    <div className="space-y-6">
      <header>
        <h2 className="text-xl font-bold text-[#eef6f3] sm:text-2xl">
          Danh mục mặt hàng chuẩn
        </h2>
        <p className="mt-2 text-sm text-[#8aa89c]" role="status">
          {status}
        </p>
      </header>

      <section
        className="space-y-4 rounded-xl border border-[#2a3d36] bg-[#151f1c] p-6"
        aria-labelledby="catalog-list-heading"
      >
        <h3 id="catalog-list-heading" className="text-base font-bold text-[#8aa89c]">
          Danh sách mặt hàng
        </h3>

        <div className="flex flex-wrap gap-2" role="toolbar" aria-label="Thao tác danh mục">
          <button
            type="button"
            onClick={onRefresh}
            className="rounded-lg bg-[#1f3d34] px-4 py-2.5 text-sm font-semibold text-[#eef6f3] transition-colors hover:bg-[#2a5248]"
          >
            <RefreshCw className="mr-2 inline h-4 w-4" aria-hidden />
            Làm mới
          </button>
          <button
            type="button"
            onClick={onAdd}
            disabled={!isFormValid}
            className="rounded-lg bg-[#0d9488] px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-[#14b8a6] disabled:opacity-50"
          >
            Thêm
          </button>
          <button
            type="button"
            onClick={onUpdate}
            disabled={!isFormValid}
            className="rounded-lg bg-[#1f3d34] px-4 py-2.5 text-sm font-semibold text-[#eef6f3] transition-colors hover:bg-[#2a5248] disabled:opacity-50"
          >
            Cập nhật
          </button>
          <button
            type="button"
            onClick={onDelete}
            className="rounded-lg border border-[#7f1d1d] px-4 py-2.5 text-sm font-semibold text-[#f87171] transition-colors hover:bg-[#3f1515]"
          >
            Xóa
          </button>
        </div>

        {sortedItems.length === 0 ? (
          <EmptyState
            title="Chưa có mặt hàng trong danh mục"
            description="Dùng form Thêm/Sửa Mặt hàng bên dưới để thêm mã mới."
          />
        ) : (
          <div className="w-full overflow-hidden rounded-lg border border-[#2a3d36] bg-[#111827]">
            <table className="w-full table-fixed border-collapse text-left text-sm">
              <colgroup>
                <col style={{ width: "15%" }} />
                <col style={{ width: "35%" }} />
                <col style={{ width: "50%" }} />
              </colgroup>
              <thead>
                <tr className="border-b border-[#2a3d36] bg-[#152820]">
                  <th className="px-4 py-3.5 font-bold text-[#8aa89c]">Mã</th>
                  <th className="px-4 py-3.5 font-bold text-[#8aa89c]">Tên</th>
                  <th className="px-4 py-3.5 font-bold text-[#8aa89c]">Mô tả</th>
                </tr>
              </thead>
              <tbody>
                {sortedItems.map((row, index) => {
                  const selected = selectedCode === row.merchandiseCode;
                  const desc = row.description ?? "—";
                  return (
                    <tr
                      key={row.merchandiseCode}
                      onClick={() => selectRow(row)}
                      className={`cursor-pointer border-b border-[#2a3d36] transition-colors ${
                        index % 2 === 1 ? "bg-[#0f1412]" : "bg-[#101916]"
                      } ${selected ? "bg-[#1f3d34]" : "hover:bg-[#1f3d34]/80"}`}
                    >
                      <td className="px-4 py-3 font-mono text-sm font-semibold text-[#eef6f3]">
                        {row.merchandiseCode}
                      </td>
                      <td
                        className="truncate px-4 py-3 font-medium text-[#eef6f3]"
                        title={row.merchandiseName}
                      >
                        {row.merchandiseName}
                      </td>
                      <td
                        className="truncate px-4 py-3 text-[#c8ddd6]"
                        title={desc}
                      >
                        {desc}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <section className="space-y-6 rounded-xl border border-[#2a3d36] bg-[#151f1c] p-6">
        <h3 className="text-base font-bold text-[#8aa89c]">Thêm/Sửa Mặt hàng</h3>
        <div className="grid max-w-2xl gap-6">
          <div>
            <label htmlFor="catalog-code" className="mb-1.5 block text-sm font-bold text-[#eef6f3]">
              Mã
            </label>
            <input
              id="catalog-code"
              type="text"
              className="w-full rounded-lg border border-[#2a3d36] bg-[#151f1c] px-3 py-2.5 text-sm text-[#eef6f3]"
              value={code}
              onChange={(e) => setCode(e.target.value)}
              placeholder="vd. P001"
            />
          </div>
          <div>
            <label htmlFor="catalog-name" className="mb-1.5 block text-sm font-bold text-[#eef6f3]">
              Tên
            </label>
            <input
              id="catalog-name"
              type="text"
              className="w-full rounded-lg border border-[#2a3d36] bg-[#151f1c] px-3 py-2.5 text-sm text-[#eef6f3]"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Tên mặt hàng"
            />
          </div>
          <div>
            <label htmlFor="catalog-desc" className="mb-1.5 block text-sm font-bold text-[#eef6f3]">
              Mô tả
            </label>
            <textarea
              id="catalog-desc"
              rows={3}
              className="w-full resize-y rounded-lg border border-[#2a3d36] bg-[#151f1c] px-3 py-2.5 text-sm text-[#eef6f3]"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Mô tả ngắn (tùy chọn)"
            />
          </div>
        </div>
      </section>
    </div>
  );
}
