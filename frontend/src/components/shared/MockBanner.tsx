import { Info } from 'lucide-react';

export function MockBanner() {
  return (
    <div className="rounded-xl border border-amber-200 dark:border-amber-900/50 bg-amber-50/50 dark:bg-amber-950/20 px-4 py-2.5 flex items-center gap-2">
      <Info className="h-4 w-4 text-amber-600 shrink-0" />
      <p className="text-xs text-amber-700 dark:text-amber-400">
        Showing demo data — backend is not connected.
        Start the Spring Boot server to see live data.
      </p>
    </div>
  );
}
