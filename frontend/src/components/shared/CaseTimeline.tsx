import type { AuditLog } from '@/types';
import { motion } from 'framer-motion';
import {
  PlusCircle, UserPlus, ArrowRightLeft, MessageSquare, Pencil,
} from 'lucide-react';

const iconMap: Record<string, React.ComponentType<{ className?: string }>> = {
  INCIDENT_CREATED: PlusCircle,
  INCIDENT_ASSIGNED: UserPlus,
  STATUS_CHANGED: ArrowRightLeft,
  COMMENT_ADDED: MessageSquare,
  INCIDENT_UPDATED: Pencil,
};

function formatTime(iso: string) {
  return new Date(iso).toLocaleString('en-GB', {
    day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit',
  });
}

export function CaseTimeline({ logs }: { logs: AuditLog[] }) {
  return (
    <div className="relative space-y-0">
      <div className="absolute left-[19px] top-3 bottom-3 w-px bg-border" />
      {logs.map((log, i) => {
        const Icon = iconMap[log.action] || PlusCircle;
        return (
          <motion.div
            key={log.id}
            initial={{ opacity: 0, x: -8 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: i * 0.05, duration: 0.3 }}
            className="relative flex gap-4 py-3"
          >
            <div className="relative z-10 flex h-10 w-10 shrink-0 items-center justify-center rounded-full border bg-card">
              <Icon className="h-4 w-4 text-muted-foreground" />
            </div>
            <div className="flex-1 pt-1">
              <p className="text-sm font-medium">{log.message}</p>
              <div className="flex items-center gap-2 mt-1">
                <span className="text-xs text-muted-foreground">{log.actor.name}</span>
                <span className="text-xs text-muted-foreground/50">·</span>
                <span className="text-xs text-muted-foreground">{formatTime(log.createdAt)}</span>
              </div>
              {log.oldValue && log.newValue && (
                <div className="mt-1.5 flex items-center gap-1.5 text-xs">
                  <span className="rounded bg-muted px-1.5 py-0.5 font-mono">{log.oldValue}</span>
                  <span className="text-muted-foreground">→</span>
                  <span className="rounded bg-muted px-1.5 py-0.5 font-mono">{log.newValue}</span>
                </div>
              )}
            </div>
          </motion.div>
        );
      })}
    </div>
  );
}
