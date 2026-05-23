import { Bell, Menu } from "lucide-react";
import { CURRENT_USER } from "../data/mockData";

/** Topbar sáng — vai trò "Bộ phận Bán hàng" khi xem danh mục */
export default function Topbar({ onMenuClick, pageTitle, roleLabel }) {
  return (
    <header className="sticky top-0 z-30 border-b border-gray-200 bg-surface/95 backdrop-blur-md">
      <div className="flex h-16 items-center gap-4 px-4 sm:px-6 lg:px-8">
        <button
          type="button"
          onClick={onMenuClick}
          className="btn-ghost rounded-lg p-2 lg:hidden"
          aria-label="Mở menu"
        >
          <Menu className="h-5 w-5" />
        </button>

        <div className="min-w-0 flex-1">
          <h1 className="truncate text-lg font-bold text-ink sm:text-xl">
            {roleLabel ?? pageTitle}
          </h1>
          {roleLabel && (
            <p className="truncate text-sm text-ink-secondary">{pageTitle}</p>
          )}
        </div>

        <div className="flex items-center gap-2 sm:gap-3">
          <button
            type="button"
            className="relative btn-ghost rounded-lg p-2.5"
            aria-label="Thông báo"
          >
            <Bell className="h-5 w-5 text-ink-secondary" />
            <span className="absolute right-2 top-2 h-2 w-2 rounded-full bg-accent ring-2 ring-surface" />
          </button>

          <div className="flex items-center gap-3 border-l border-gray-200 pl-3 sm:pl-4">
            <div className="hidden text-right sm:block">
              <p className="text-sm font-semibold text-ink">{CURRENT_USER.name}</p>
              <p className="text-xs text-ink-secondary">{CURRENT_USER.role}</p>
            </div>
            <div
              className="flex h-10 w-10 items-center justify-center rounded-full bg-accent text-sm font-bold text-white shadow-sm transition-transform duration-200 hover:scale-105"
              title={CURRENT_USER.email}
            >
              {CURRENT_USER.initials}
            </div>
          </div>
        </div>
      </div>
    </header>
  );
}
