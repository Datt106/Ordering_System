import {
  CheckCircle2,
  Clock,
  Globe2,
  Loader,
  TrendingUp,
  AlertTriangle,
} from "lucide-react";

const ICONS = { Clock, Loader, Globe2, CheckCircle2 };

const TREND_STYLES = {
  up: { icon: TrendingUp, className: "text-accent" },
  warn: { icon: AlertTriangle, className: "text-amber-600" },
  neutral: { icon: null, className: "text-ink-muted" },
};

export default function StatCard({ label, value, change, trend, icon }) {
  const Icon = ICONS[icon] ?? Clock;
  const trendMeta = TREND_STYLES[trend] ?? TREND_STYLES.neutral;
  const TrendIcon = trendMeta.icon;

  return (
    <article className="card transition-shadow duration-200 hover:shadow-cardHover">
      <div className="flex items-start justify-between gap-4">
        <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-accent-muted text-accent">
          <Icon className="h-5 w-5" aria-hidden />
        </div>
        {TrendIcon && (
          <TrendIcon className={`h-4 w-4 ${trendMeta.className}`} aria-hidden />
        )}
      </div>
      <p className="mt-4 text-sm font-medium text-ink-secondary">{label}</p>
      <p className="mt-1 text-3xl font-bold tracking-tight text-ink">{value}</p>
      <p className={`mt-2 text-xs font-medium ${trendMeta.className}`}>{change}</p>
    </article>
  );
}
