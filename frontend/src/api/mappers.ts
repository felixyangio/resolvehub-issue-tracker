import type {
  IncidentResponse,
  CommentResponse,
  AuditLogResponse,
  DashboardSummaryResponse,
} from './endpoints';
import type {
  Incident,
  Comment,
  AuditLog,
  DashboardSummary,
  IncidentCategory,
  Priority,
  IncidentStatus,
  AuditAction,
  Role,
} from '@/types';

/**
 * Map backend IncidentResponse (flat fields) to frontend Incident (nested User objects).
 */
export function mapIncident(r: IncidentResponse): Incident {
  return {
    id: r.id,
    title: r.title,
    description: r.description,
    category: r.category as IncidentCategory,
    priority: r.priority as Priority,
    status: r.status as IncidentStatus,
    createdBy: {
      id: r.createdById,
      name: r.createdByName,
      email: '',
      role: (r.createdByRole as Role) ?? 'USER',
    },
    assignedTo: r.assignedToId
      ? {
          id: r.assignedToId,
          name: r.assignedToName ?? 'Unknown',
          email: '',
          role: (r.assignedToRole as Role) ?? 'AGENT',
        }
      : undefined,
    dueAt: r.dueAt ?? undefined,
    createdAt: r.createdAt,
    updatedAt: r.updatedAt,
  };
}

/**
 * Map backend CommentResponse (flat fields) to frontend Comment (nested author).
 */
export function mapComment(r: CommentResponse): Comment {
  return {
    id: r.id,
    content: r.content,
    author: {
      id: r.authorId,
      name: r.authorName,
      email: '',
      role: r.authorRole as Role,
    },
    authorRole: r.authorRole as Role,
    createdAt: r.createdAt,
  };
}

/**
 * Map backend AuditLogResponse (flat fields) to frontend AuditLog (nested actor).
 */
export function mapAuditLog(r: AuditLogResponse): AuditLog {
  return {
    id: r.id,
    action: r.action as AuditAction,
    actor: {
      id: r.actorId,
      name: r.actorName,
      email: '',
      role: (r.actorRole as Role) ?? 'USER',
    },
    oldValue: r.oldValue ?? undefined,
    newValue: r.newValue ?? undefined,
    message: r.message ?? '',
    createdAt: r.createdAt,
  };
}

/**
 * Map backend DashboardSummaryResponse to frontend DashboardSummary.
 * Some frontend fields don't have direct backend equivalents;
 * we derive reasonable values from what the backend provides.
 */
export function mapDashboardSummary(r: DashboardSummaryResponse): DashboardSummary {
  return {
    totalCases: r.totalIncidents,
    openCases: r.openIncidents + r.assignedIncidents + r.inProgressIncidents,
    resolvedToday: r.resolvedIncidents + r.closedIncidents,
    avgResolutionHours: 0,
    criticalOpen: r.criticalIncidents,
    assignedToMe: r.assignedIncidents,
  };
}
