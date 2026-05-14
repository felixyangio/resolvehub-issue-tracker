export type Role = 'USER' | 'AGENT' | 'MANAGER' | 'ADMIN';

export type IncidentStatus = 'NEW' | 'ASSIGNED' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED' | 'CANCELLED';

export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export type IncidentCategory =
  | 'MAINTENANCE'
  | 'SAFETY'
  | 'NOISE'
  | 'INTERNET'
  | 'BILLING'
  | 'DEPOSIT'
  | 'CLEANING'
  | 'ACCESS'
  | 'OTHER';

export type AuditAction =
  | 'INCIDENT_CREATED'
  | 'INCIDENT_UPDATED'
  | 'INCIDENT_ASSIGNED'
  | 'STATUS_CHANGED'
  | 'COMMENT_ADDED';

export interface User {
  id: string;
  name: string;
  email: string;
  role: Role;
  avatarUrl?: string;
}

export interface Incident {
  id: string;
  title: string;
  description: string;
  category: IncidentCategory;
  priority: Priority;
  status: IncidentStatus;
  createdBy: User;
  assignedTo?: User;
  dueAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface Comment {
  id: string;
  content: string;
  author: User;
  authorRole: Role;
  createdAt: string;
}

export interface AuditLog {
  id: string;
  action: AuditAction;
  actor: User;
  oldValue?: string;
  newValue?: string;
  message: string;
  createdAt: string;
}

export interface DashboardSummary {
  totalCases: number;
  openCases: number;
  resolvedToday: number;
  avgResolutionHours: number;
  criticalOpen: number;
  assignedToMe: number;
}

export interface StatusCount {
  status: IncidentStatus;
  count: number;
}

export interface CategoryCount {
  category: IncidentCategory;
  count: number;
}

export interface PriorityCount {
  priority: Priority;
  count: number;
}
