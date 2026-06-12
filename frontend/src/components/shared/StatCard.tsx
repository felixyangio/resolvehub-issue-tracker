import { motion } from 'framer-motion';
import type { LucideIcon } from 'lucide-react';

interface StatCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon: LucideIcon;
  trend?: { value: number; label: string };
  accent?: string;
}

export function StatCard({ title, value, subtitle, icon: Icon, trend, accent = 'text-foreground' }: StatCardProps) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 12 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, ease: [0.25, 0.46, 0.45, 0.94] }}
      className="group relative overflow-hidden rounded-2xl border border-border/50 bg-card p-6 transition-all hover:border-border hover:shadow-sm"
    >
      <div className="flex items-start justify-between">
        <div className="space-y-3">
          <p className="text-sm font-medium text-muted-foreground">{title}</p>
          <p className={`text-3xl font-semibold tracking-tight ${accent}`}>{value}</p>
          {subtitle && <p className="text-xs text-muted-foreground">{subtitle}</p>}
          {trend && (
            <p className={`text-xs font-medium ${trend.value >= 0 ? 'text-emerald-600' : 'text-red-500'}`}>
              {trend.value >= 0 ? '+' : ''}{trend.value}% {trend.label}
            </p>
          )}
        </div>
        <div className="rounded-xl bg-muted/50 p-3">
          <Icon className="h-5 w-5 text-muted-foreground" />
        </div>
      </div>
    </motion.div>
  );
}
