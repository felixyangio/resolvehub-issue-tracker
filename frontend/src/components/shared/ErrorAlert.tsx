import { AlertTriangle, RefreshCcw } from 'lucide-react';
import { Button } from '@/components/ui/button';

interface ErrorAlertProps {
  message: string;
  onRetry?: () => void;
}

export function ErrorAlert({ message, onRetry }: ErrorAlertProps) {
  return (
    <div className="rounded-2xl border border-red-200 dark:border-red-900/50 bg-red-50/50 dark:bg-red-950/20 p-6">
      <div className="flex items-start gap-3">
        <AlertTriangle className="h-5 w-5 text-red-600 shrink-0 mt-0.5" />
        <div className="flex-1 space-y-2">
          <p className="text-sm font-medium text-red-700 dark:text-red-400">
            Something went wrong
          </p>
          <p className="text-sm text-red-600/80 dark:text-red-400/80">{message}</p>
          {onRetry && (
            <Button
              variant="outline"
              size="sm"
              className="rounded-xl mt-2 border-red-200 text-red-600 hover:bg-red-50 dark:border-red-900 dark:hover:bg-red-950/30"
              onClick={onRetry}
            >
              <RefreshCcw className="mr-2 h-3 w-3" />
              Try Again
            </Button>
          )}
        </div>
      </div>
    </div>
  );
}
