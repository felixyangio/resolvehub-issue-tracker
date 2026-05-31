import type { Role, IncidentStatus } from '@/types';

export const ROLE_LABELS: Record<Role, string> = {
  USER: 'Resident',
  AGENT: 'Maintenance Staff',
  MANAGER: 'Property Manager',
  ADMIN: 'Admin',
};

/** Short role labels suitable for compact UI elements like comment badges */
export const ROLE_LABELS_SHORT: Record<Role, string> = {
  USER: 'Resident',
  AGENT: 'Staff',
  MANAGER: 'Manager',
  ADMIN: 'Admin',
};

export const STATUS_LABELS: Record<IncidentStatus, string> = {
  NEW: 'New',
  ASSIGNED: 'Assigned',
  IN_PROGRESS: 'In Progress',
  RESOLVED: 'Resolved',
  CLOSED: 'Closed',
  CANCELLED: 'Cancelled',
};
