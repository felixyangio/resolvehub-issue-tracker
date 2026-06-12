import { Badge } from '@/components/ui/badge';
import type { IncidentStatus } from '@/types';
import { cn } from '@/lib/utils';

const config: Record<IncidentStatus, { label: string; className: string }> = {
  NEW: {
    label: 'New',
    className: 'bg-blue-50 text-blue-700 border-blue-200 dark:bg-blue-950/50 dark:text-blue-300 dark:border-blue-800',
  },
  ASSIGNED: {
    label: 'Assigned',
    className: 'bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-950/50 dark:text-amber-300 dark:border-amber-800',
  },
  IN_PROGRESS: {
    label: 'In Progress',
    className: 'bg-violet-50 text-violet-700 border-violet-200 dark:bg-violet-950/50 dark:text-violet-300 dark:border-violet-800',
  },
  RESOLVED: {
    label: 'Resolved',
    className: 'bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/50 dark:text-emerald-300 dark:border-emerald-800',
  },
  CLOSED: {
    label: 'Closed',
    className: 'bg-zinc-100 text-zinc-500 border-zinc-200 dark:bg-zinc-800/50 dark:text-zinc-400 dark:border-zinc-700',
  },
  CANCELLED: {
    label: 'Cancelled',
    className: 'bg-red-50 text-red-600 border-red-200 dark:bg-red-950/50 dark:text-red-400 dark:border-red-800',
  },
};

export function StatusBadge({ status }: { status: IncidentStatus }) {
  const c = config[status];
  return (
    <Badge variant="outline" className={cn('font-medium text-xs px-2.5 py-0.5 rounded-full border', c.className)}>
      {c.label}
    </Badge>
  );
}
