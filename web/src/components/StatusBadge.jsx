/**
 * Badge trạng thái — màu theo ngữ cảnh (Nielsen: recognition over recall).
 */
const TONE_CLASSES = {
  emerald: "bg-emerald-50 text-emerald-700 ring-emerald-600/20",
  amber: "bg-amber-50 text-amber-800 ring-amber-600/20",
  blue: "bg-blue-50 text-blue-700 ring-blue-600/20",
  rose: "bg-rose-50 text-rose-700 ring-rose-600/20",
};

export default function StatusBadge({ label, tone = "emerald" }) {
  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ring-inset ${TONE_CLASSES[tone] ?? TONE_CLASSES.emerald}`}
    >
      {label}
    </span>
  );
}
