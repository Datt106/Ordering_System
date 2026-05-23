import { useState } from "react";
import Sidebar from "./Sidebar";
import Topbar from "./Topbar";
import StatCard from "./StatCard";
import ImportRequestsTable from "./ImportRequestsTable";
import CatalogPage from "./CatalogPage";
import EmptyState from "./EmptyState";
import { STATS, PAGE_TITLES } from "../data/mockData";

export default function DashboardLayout() {
  const [activeNav, setActiveNav] = useState("catalog");
  const [mobileOpen, setMobileOpen] = useState(false);

  const handleNavigate = (id) => {
    setActiveNav(id);
    setMobileOpen(false);
  };

  const pageTitle = PAGE_TITLES[activeNav] ?? "Tổng quan";
  const showSalesHeader = activeNav === "catalog";

  return (
    <div className="flex min-h-screen bg-surface-muted">
      <Sidebar
        activeId={activeNav}
        onNavigate={handleNavigate}
        mobileOpen={mobileOpen}
        onCloseMobile={() => setMobileOpen(false)}
      />

      <div className="flex min-w-0 flex-1 flex-col">
        <Topbar
          pageTitle={pageTitle}
          roleLabel={showSalesHeader ? "Bộ phận Bán hàng" : null}
          onMenuClick={() => setMobileOpen(true)}
        />

        <main className="flex-1 bg-surface-muted p-6 lg:p-8">
          {activeNav === "dashboard" && (
            <div className="mx-auto max-w-7xl space-y-8">
              <section
                aria-label="Chỉ số tổng quan"
                className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4"
              >
                {STATS.map((stat) => (
                  <StatCard key={stat.id} {...stat} />
                ))}
              </section>
              <ImportRequestsTable />
            </div>
          )}

          {activeNav === "catalog" && (
            <div className="mx-auto max-w-5xl">
              <CatalogPage />
            </div>
          )}

          {activeNav !== "dashboard" && activeNav !== "catalog" && (
            <div className="mx-auto max-w-3xl">
              <section className="card">
                <EmptyState
                  title={`${pageTitle} — đang phát triển`}
                  description="Màn hình sẽ kết nối API backend trong giai đoạn tiếp theo."
                  actionLabel="Về Danh mục mặt hàng chuẩn"
                  onAction={() => handleNavigate("catalog")}
                />
              </section>
            </div>
          )}
        </main>

        <footer className="border-t border-gray-200 bg-surface px-6 py-4 text-center text-xs text-ink-muted">
          Ordering System © 2026
        </footer>
      </div>
    </div>
  );
}
