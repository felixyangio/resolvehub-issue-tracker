import { Badge } from '@/components/ui/badge';
import type { IncidentCategory } from '@/types';
import {
  Wrench, ShieldAlert, Volume2, Wifi, Receipt,
  Landmark, SprayCan, KeyRound, HelpCircle,
} from 'lucide-react';

const config: Record<IncidentCategory, { label: string; icon: React.ComponentType<{ className?: string }> }> = {
  MAINTENANCE: { label: 'Maintenance', icon: Wrench },
  SAFETY: { label: 'Safety', icon: ShieldAlert },
  NOISE: { label: 'Noise', icon: Volume2 },
  INTERNET: { label: 'Internet', icon: Wifi },
  BILLING: { label: 'Billing', icon: Receipt },
  DEPOSIT: { label: 'Deposit', icon: Landmark },
  CLEANING: { label: 'Cleaning', icon: SprayCan },
  ACCESS: { label: 'Access', icon: KeyRound },
  OTHER: { label: 'Other', icon: HelpCircle },
};

export function CategoryBadge({ category }: { category: IncidentCategory }) {
  const c = config[category];
  const Icon = c.icon;
  return (
    <Badge variant="secondary" className="font-medium text-xs gap-1 rounded-full">
      <Icon className="h-3 w-3" />
      {c.label}
    </Badge>
  );
}
