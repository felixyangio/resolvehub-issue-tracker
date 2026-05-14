import type { Role } from '@/types';

export const ROLE_LABELS: Record<Role, string> = {
  USER: 'Resident',
  AGENT: 'Maintenance Staff',
  MANAGER: 'Property Manager',
  ADMIN: 'Admin',
};
