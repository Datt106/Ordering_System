import {
  Boxes,
  FileText,
  Globe2,
  LayoutDashboard,
  PackageSearch,
  Package,
  Settings,
  X,
} from "lucide-react";
import { NAV_ITEMS } from "../data/mockData";

const ICON_MAP = {
  LayoutDashboard,
  FileText,
  Globe2,
  PackageSearch,
  Boxes,
};

/** Sidebar sáng — #F9FAFB, active emerald */
export default function Sidebar({
  activeId,
  onNavigate,
  mobileOpen,
  onCloseMobile,
}) {
  return (
    <>
      {mobileOpen && (
        <button
          type="button"
          className="fixed inset-0 z-40 bg-ink/40 backdrop-blur-sm lg:hidden"
          onClick={onCloseMobile}
          aria-label="Đóng menu"
        />
      )}

      <aside
        className={`fixed inset-y-0 left-0 z-50 flex w-72 flex-col border-r border-gray-200 bg-surface-muted transition-transform duration-300 ease-out lg:static lg:translate-x-0 ${
          mobileOpen ? "translate-x-0" : "-translate-x-full"
        }`}
        aria-label="Điều hướng chính"
      >
        <div className="flex h-16 items-center justify-between gap-3 border-b border-gray-200 bg-surface px-5">
          <div className="flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-accent text-white shadow-sm">
              <Package className="h-5 w-5" aria-hidden />
            </div>
            <div>
              <p className="text-sm font-bold leading-tight text-ink">Ordering</p>
              <p className="text-[10px] font-medium uppercase tracking-wider text-ink-muted">
                Import System
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onCloseMobile}
            className="rounded-lg p-2 text-ink-muted transition-colors hover:bg-gray-100 lg:hidden"
            aria-label="Đóng sidebar"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <nav className="flex-1 space-y-1 overflow-y-auto px-4 py-6">
          <p className="mb-3 px-2 text-[10px] font-semibold uppercase tracking-wider text-ink-muted">
            Chức năng
          </p>
          {NAV_ITEMS.map((item) => {
            const Icon = ICON_MAP[item.icon];
            const isActive = activeId === item.id;
            return (
              <button
                key={item.id}
                type="button"
                onClick={() => onNavigate(item.id)}
                className={`nav-item ${isActive ? "nav-item-active" : ""}`}
                aria-current={isActive ? "page" : undefined}
              >
                {Icon && <Icon className="h-5 w-5 shrink-0" aria-hidden />}
                {item.label}
              </button>
            );
          })}
        </nav>

        <div className="border-t border-gray-200 bg-surface p-4">
          <button type="button" className="nav-item w-full">
            <Settings className="h-5 w-5 shrink-0" aria-hidden />
            Cài đặt
          </button>
        </div>
      </aside>
    </>
  );
}
