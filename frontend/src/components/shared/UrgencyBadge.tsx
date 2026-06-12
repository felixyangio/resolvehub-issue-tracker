import { Badge } from '@/components/ui/badge';
import type { Priority } from '@/types';
import { cn } from '@/lib/utils';
import { AlertTriangle, AlertCircle, Minus, ArrowDown } from 'lucide-react';

const config: Record<Priority, { label: string; className: string; icon: React.ComponentType<{ className?: string }> }> = {
  CRITICAL: {
    label: 'Critical',
    className: 'bg-red-50 text-red-700 border-red-200 dark:bg-red-950/50 dark:text-red-300 dark:border-red-800',
    icon: AlertTriangle,
  },
  HIGH: {
    label: 'High',
    className: 'bg-orange-50 text-orange-700 border-orange-200 dark:bg-orange-950/50 dark:text-orange-300 dark:border-orange-800',
    icon: AlertCircle,
  },
  MEDIUM: {
    label: 'Medium',
    className: 'bg-yellow-50 text-yellow-700 border-yellow-200 dark:bg-yellow-950/50 dark:text-yellow-300 dark:border-yellow-800',
    icon: Minus,
  },
  LOW: {
    label: 'Low',
    className: 'bg-sky-50 text-sky-700 border-sky-200 dark:bg-sky-950/50 dark:text-sky-300 dark:border-sky-800',
    icon: ArrowDown,
  },
};

export function UrgencyBadge({ priority }: { priority: Priority }) {
  const c = config[priority];
  const Icon = c.icon;
  return (
    <Badge variant="outline" className={cn('font-medium text-xs px-2.5 py-0.5 rounded-full border gap-1', c.className)}>
      <Icon className="h-3 w-3" />
      {c.label}
    </Badge>
  );
}
