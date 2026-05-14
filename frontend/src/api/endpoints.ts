import { api } from './client';
import type {
  IncidentCategory, Priority, IncidentStatus,
} from '@/types';

// --- Backend response types (match Spring Boot DTOs exactly) ---

export interface AuthResponse {
  token: string;
  email: string;
  role: string;
}

export interface UserResponse {
  id: string;
  name: string;
  email: string;
  role: string;
  enabled: boolean;
  createdAt: string;
}

export interface IncidentResponse {
  id: string;
  title: string;
  description: string;
  category: string;
  priority: string;
  status: string;
  createdById: string;
  createdByName: string;
  createdByRole: string | null;
  assignedToId: string | null;
  assignedToName: string | null;
  assignedToRole: string | null;
  dueAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CommentResponse {
  id: string;
  content: string;
  authorId: string;
  authorName: string;
  authorRole: string;
  createdAt: string;
}

export interface AuditLogResponse {
  id: string;
  action: string;
  oldValue: string | null;
  newValue: string | null;
  message: string | null;
  actorId: string;
  actorName: string;
  actorRole: string | null;
  createdAt: string;
}

export interface WeeklyTrendResponse {
  day: string;
  created: number;
  resolved: number;
}

export interface DashboardSummaryResponse {
  totalIncidents: number;
  openIncidents: number;
  assignedIncidents: number;
  inProgressIncidents: number;
  resolvedIncidents: number;
  closedIncidents: number;
  criticalIncidents: number;
  highIncidents: number;
}

export interface StatusCountResponse {
  status: string;
  count: number;
}

export interface PriorityCountResponse {
  priority: string;
  count: number;
}

export interface Page<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

// --- Auth ---

export const authApi = {
  login: (email: string, password: string) =>
    api.post<AuthResponse>('/auth/login', { email, password }),

  register: (name: string, email: string, password: string) =>
    api.post<AuthResponse>('/auth/register', { name, email, password }),

  me: () => api.get<UserResponse>('/users/me'),

  agents: () => api.get<UserResponse[]>('/users/agents'),
};

// --- Incidents ---

export const incidentApi = {
  list: (params?: { status?: IncidentStatus; category?: IncidentCategory; priority?: Priority; page?: number; size?: number }) => {
    const query = new URLSearchParams();
    if (params?.status) query.set('status', params.status);
    if (params?.category) query.set('category', params.category);
    if (params?.priority) query.set('priority', params.priority);
    if (params?.page !== undefined) query.set('page', String(params.page));
    if (params?.size) query.set('size', String(params.size));
    const qs = query.toString();
    return api.get<Page<IncidentResponse>>(`/incidents${qs ? `?${qs}` : ''}`);
  },

  get: (id: string) => api.get<IncidentResponse>(`/incidents/${id}`),

  create: (data: { title: string; description: string; category?: IncidentCategory; priority?: Priority }) =>
    api.post<IncidentResponse>('/incidents', data),

  update: (id: string, data: { title?: string; description?: string; category?: IncidentCategory; priority?: Priority }) =>
    api.put<IncidentResponse>(`/incidents/${id}`, data),

  assign: (id: string, agentId: string) =>
    api.patch<IncidentResponse>(`/incidents/${id}/assign`, { agentId }),

  updateStatus: (id: string, status: IncidentStatus) =>
    api.patch<IncidentResponse>(`/incidents/${id}/status`, { status }),

  delete: (id: string) => api.delete<void>(`/incidents/${id}`),
};

// --- Comments ---

export const commentApi = {
  list: (incidentId: string) =>
    api.get<CommentResponse[]>(`/incidents/${incidentId}/comments`),

  create: (incidentId: string, content: string) =>
    api.post<CommentResponse>(`/incidents/${incidentId}/comments`, { content }),
};

// --- Audit Logs ---

export const auditLogApi = {
  list: (incidentId: string) =>
    api.get<AuditLogResponse[]>(`/incidents/${incidentId}/audit-logs`),
};

// --- Dashboard ---

export interface CategoryCountResponse {
  category: string;
  count: number;
}

export const dashboardApi = {
  summary: () => api.get<DashboardSummaryResponse>('/dashboard/summary'),
  byStatus: () => api.get<StatusCountResponse[]>('/dashboard/incidents-by-status'),
  bySeverity: () => api.get<PriorityCountResponse[]>('/dashboard/incidents-by-severity'),
  byCategory: () => api.get<CategoryCountResponse[]>('/dashboard/incidents-by-category'),
  recentActivity: () => api.get<IncidentResponse[]>('/dashboard/recent-activity'),
  weeklyTrend: () => api.get<WeeklyTrendResponse[]>('/dashboard/weekly-trend'),
};
