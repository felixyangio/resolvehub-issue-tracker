import { useState, useMemo } from 'react';
import { useParams, Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowLeft, Clock, User, UserCheck, Calendar, Send, Loader2, ArrowRightLeft, CheckCircle2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Separator } from '@/components/ui/separator';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { StatusBadge } from '@/components/shared/StatusBadge';
import { UrgencyBadge } from '@/components/shared/UrgencyBadge';
import { CategoryBadge } from '@/components/shared/CategoryBadge';
import { CaseTimeline } from '@/components/shared/CaseTimeline';
import { CommentThread } from '@/components/shared/CommentThread';
import { EmptyState } from '@/components/shared/EmptyState';
import { PageLoader } from '@/components/shared/PageLoader';
import { ErrorAlert } from '@/components/shared/ErrorAlert';
import { MockBanner } from '@/components/shared/MockBanner';
import { useApi, useMutation } from '@/hooks/useApi';
import { incidentApi, commentApi, auditLogApi, authApi } from '@/api/endpoints';
import type { UserResponse } from '@/api/endpoints';
import { mapIncident, mapComment, mapAuditLog } from '@/api/mappers';
import { incidents as mockIncidents, commentsForCase as mockComments, auditLogsForCase as mockAuditLogs, users as mockUsers } from '@/data/mock';
import { useAuth } from '@/contexts/AuthContext';
import type { Incident, Comment as CommentType, AuditLog, IncidentStatus } from '@/types';

function formatDate(iso: string) {
  return new Date(iso).toLocaleString('en-GB', {
    day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit',
  });
}

const EMPTY_COMMENTS: CommentType[] = [];
const EMPTY_AUDIT_LOGS: AuditLog[] = [];

const ALL_TRANSITIONS: Record<IncidentStatus, { value: IncidentStatus; label: string; managerOnly?: boolean }[]> = {
  NEW: [
    { value: 'ASSIGNED', label: 'Assign', managerOnly: true },
    { value: 'CANCELLED', label: 'Cancel', managerOnly: true },
  ],
  ASSIGNED: [
    { value: 'IN_PROGRESS', label: 'Start Work' },
    { value: 'CANCELLED', label: 'Cancel', managerOnly: true },
  ],
  IN_PROGRESS: [
    { value: 'RESOLVED', label: 'Mark Resolved' },
    { value: 'CANCELLED', label: 'Cancel', managerOnly: true },
  ],
  RESOLVED: [
    { value: 'CLOSED', label: 'Close Case', managerOnly: true },
  ],
  CLOSED: [],
  CANCELLED: [],
};

function getTransitionsForRole(status: IncidentStatus, isManager: boolean) {
  const transitions = ALL_TRANSITIONS[status] ?? [];
  if (isManager) return transitions;
  return transitions.filter(t => !t.managerOnly);
}

const mockAgents = mockUsers.filter(u => u.role === 'AGENT');

export function CaseDetail() {
  const { id } = useParams();
  const { user } = useAuth();
  const [commentText, setCommentText] = useState('');
  const [statusSuccess, setStatusSuccess] = useState<string | null>(null);
  const [assignSuccess, setAssignSuccess] = useState<string | null>(null);

  const canManage = user?.role === 'MANAGER' || user?.role === 'ADMIN';
  const isAgent = user?.role === 'AGENT';

  const mockIncident = useMemo(() => mockIncidents.find((i) => i.id === id), [id]);

  const mockCommentsData = useMemo(
    () => (id === 'INC-001' ? mockComments : EMPTY_COMMENTS),
    [id],
  );

  const mockAuditData = useMemo(() => {
    if (id === 'INC-001') return mockAuditLogs;
    if (!mockIncident) return EMPTY_AUDIT_LOGS;
    return [{
      id: 'auto-1',
      action: 'INCIDENT_CREATED' as const,
      actor: mockIncident.createdBy,
      message: 'Case created',
      createdAt: mockIncident.createdAt,
    }];
  }, [id, mockIncident]);

  // Fetch incident
  const {
    data: incident,
    isLoading: incidentLoading,
    isUsingMock,
    refetch: refetchIncident,
  } = useApi<Incident | null>(
    async () => {
      const res = await incidentApi.get(id!);
      return mapIncident(res);
    },
    [id],
    { mockData: mockIncident ?? null, skip: !id },
  );

  // Fetch comments
  const {
    data: comments,
    refetch: refetchComments,
  } = useApi<CommentType[]>(
    async () => {
      const res = await commentApi.list(id!);
      return res.map(mapComment);
    },
    [id],
    { mockData: mockCommentsData, skip: !id },
  );

  // Fetch audit logs
  const {
    data: auditLogs,
    refetch: refetchAuditLogs,
  } = useApi<AuditLog[]>(
    async () => {
      const res = await auditLogApi.list(id!);
      return res.map(mapAuditLog);
    },
    [id],
    { mockData: mockAuditData, skip: !id },
  );

  // Fetch agents for assignment (only for managers)
  const { data: agents } = useApi<UserResponse[]>(
    async () => authApi.agents(),
    [],
    { mockData: mockAgents.map(u => ({ id: u.id, name: u.name, email: u.email, role: u.role, enabled: true, createdAt: '' })), skip: !canManage },
  );

  // Status change mutation
  const changeStatus = useMutation(
    async (status: IncidentStatus) => {
      return incidentApi.updateStatus(id!, status);
    },
  );

  // Assign mutation
  const assignAgent = useMutation(
    async (agentId: string) => {
      return incidentApi.assign(id!, agentId);
    },
  );

  // Add comment mutation
  const addComment = useMutation(
    async (content: string) => {
      await commentApi.create(id!, content);
    },
  );

  const handleStatusChange = async (newStatus: string) => {
    if (!id) return;
    setStatusSuccess(null);
    try {
      await changeStatus.execute(newStatus as IncidentStatus);
      refetchIncident();
      refetchAuditLogs();
      setStatusSuccess(`Status updated to ${newStatus.replace('_', ' ')}`);
      setTimeout(() => setStatusSuccess(null), 3000);
    } catch {
      // Error is captured by useMutation and shown inline
    }
  };

  const handleAssign = async (agentId: string) => {
    if (!id) return;
    setAssignSuccess(null);
    try {
      await assignAgent.execute(agentId);
      refetchIncident();
      refetchAuditLogs();
      const agentName = agents?.find(a => a.id === agentId)?.name ?? 'agent';
      setAssignSuccess(`Assigned to ${agentName}`);
      setTimeout(() => setAssignSuccess(null), 3000);
    } catch {
      // Error is captured by useMutation and shown inline
    }
  };

  const handleAddComment = async () => {
    if (!commentText.trim() || !id) return;
    try {
      await addComment.execute(commentText.trim());
      setCommentText('');
      refetchComments();
    } catch {
      // Error is captured by useMutation and shown inline
    }
  };

  if (incidentLoading) return <PageLoader message="Loading case details..." />;

  if (!incident) {
    return (
      <EmptyState
        icon={Clock}
        title="Case not found"
        description={`No case found with ID "${id}".`}
        action={<Link to="/cases"><Button variant="outline" className="rounded-xl">Back to Cases</Button></Link>}
      />
    );
  }

  const safeComments = comments ?? [];
  const safeAuditLogs = auditLogs ?? [];
  const allowedTransitions = getTransitionsForRole(incident.status, canManage);
  const isFinalStatus = incident.status === 'CLOSED' || incident.status === 'CANCELLED';

  return (
    <div className="space-y-6">
      {isUsingMock && <MockBanner />}

      {/* Back */}
      <Link to="/cases" className="inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground transition-colors">
        <ArrowLeft className="h-4 w-4" />
        Back to Cases
      </Link>

      {/* Header */}
      <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} className="space-y-4">
        <div className="flex flex-wrap items-start gap-3">
          <span className="text-xs font-mono text-muted-foreground bg-muted px-2 py-1 rounded-lg">{incident.id}</span>
          <StatusBadge status={incident.status} />
          <UrgencyBadge priority={incident.priority} />
          <CategoryBadge category={incident.category} />
        </div>
        <h2 className="text-2xl font-semibold tracking-tight">{incident.title}</h2>
      </motion.div>

      <div className="grid lg:grid-cols-3 gap-8">
        {/* Main content */}
        <div className="lg:col-span-2 space-y-6">
          {/* Description */}
          <motion.div
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 }}
            className="rounded-2xl border bg-card p-6"
          >
            <h3 className="text-sm font-semibold mb-3">Description</h3>
            <p className="text-sm text-muted-foreground leading-relaxed whitespace-pre-wrap">{incident.description}</p>
          </motion.div>

          {/* Tabs */}
          <motion.div
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
          >
            <Tabs defaultValue="comments" className="space-y-4">
              <TabsList className="bg-muted/50 rounded-xl p-1">
                <TabsTrigger value="comments" className="rounded-lg text-xs">Comments ({safeComments.length})</TabsTrigger>
                <TabsTrigger value="timeline" className="rounded-lg text-xs">Timeline ({safeAuditLogs.length})</TabsTrigger>
              </TabsList>

              <TabsContent value="comments" className="space-y-4">
                {safeComments.length > 0 ? (
                  <CommentThread comments={safeComments} />
                ) : (
                  <div className="text-center py-8 text-sm text-muted-foreground">No comments yet</div>
                )}

                <Separator />

                <div className="space-y-3">
                  <Textarea
                    placeholder="Add a comment..."
                    className="rounded-xl min-h-[80px] resize-none"
                    value={commentText}
                    onChange={(e) => setCommentText(e.target.value)}
                    disabled={addComment.isLoading}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter' && !e.shiftKey) {
                        e.preventDefault();
                        handleAddComment();
                      }
                    }}
                  />
                  {addComment.error && (
                    <p className="text-xs text-red-600">{addComment.error}</p>
                  )}
                  <div className="flex justify-end">
                    <Button
                      className="rounded-xl"
                      onClick={handleAddComment}
                      disabled={!commentText.trim() || addComment.isLoading}
                    >
                      {addComment.isLoading ? (
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      ) : (
                        <Send className="mr-2 h-4 w-4" />
                      )}
                      Send
                    </Button>
                  </div>
                </div>
              </TabsContent>

              <TabsContent value="timeline">
                <div className="rounded-2xl border bg-card p-6">
                  {safeAuditLogs.length > 0 ? (
                    <CaseTimeline logs={safeAuditLogs} />
                  ) : (
                    <div className="text-center py-8 text-sm text-muted-foreground">No activity yet</div>
                  )}
                </div>
              </TabsContent>
            </Tabs>
          </motion.div>
        </div>

        {/* Sidebar */}
        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.15 }}
          className="space-y-4"
        >
          {/* Details card */}
          <div className="rounded-2xl border bg-card p-6 space-y-5">
            <h3 className="text-sm font-semibold">Details</h3>

            <div className="space-y-4">
              <div className="flex items-center gap-3">
                <div className="h-8 w-8 rounded-lg bg-muted flex items-center justify-center shrink-0">
                  <User className="h-4 w-4 text-muted-foreground" />
                </div>
                <div>
                  <p className="text-xs text-muted-foreground">Reported by</p>
                  <p className="text-sm font-medium">{incident.createdBy.name}</p>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <div className="h-8 w-8 rounded-lg bg-muted flex items-center justify-center shrink-0">
                  <UserCheck className="h-4 w-4 text-muted-foreground" />
                </div>
                <div>
                  <p className="text-xs text-muted-foreground">Assigned to</p>
                  <p className="text-sm font-medium">{incident.assignedTo?.name ?? 'Unassigned'}</p>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <div className="h-8 w-8 rounded-lg bg-muted flex items-center justify-center shrink-0">
                  <Calendar className="h-4 w-4 text-muted-foreground" />
                </div>
                <div>
                  <p className="text-xs text-muted-foreground">Created</p>
                  <p className="text-sm font-medium">{formatDate(incident.createdAt)}</p>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <div className="h-8 w-8 rounded-lg bg-muted flex items-center justify-center shrink-0">
                  <Clock className="h-4 w-4 text-muted-foreground" />
                </div>
                <div>
                  <p className="text-xs text-muted-foreground">Last updated</p>
                  <p className="text-sm font-medium">{formatDate(incident.updatedAt)}</p>
                </div>
              </div>
            </div>
          </div>

          {/* Actions card */}
          {(canManage || isAgent) && !isFinalStatus && (
            <div className="rounded-2xl border bg-card p-6 space-y-4">
              <h3 className="text-sm font-semibold">Actions</h3>

              {/* Status change */}
              {allowedTransitions.length > 0 && (canManage || isAgent) && (
                <div className="space-y-2">
                  <p className="text-xs text-muted-foreground">Change Status</p>
                  <Select
                    onValueChange={handleStatusChange}
                    disabled={changeStatus.isLoading}
                  >
                    <SelectTrigger className="rounded-xl h-10">
                      <SelectValue placeholder={changeStatus.isLoading ? 'Updating...' : 'Select new status'} />
                    </SelectTrigger>
                    <SelectContent className="rounded-xl">
                      {allowedTransitions.map(t => (
                        <SelectItem key={t.value} value={t.value}>
                          <div className="flex items-center gap-2">
                            <ArrowRightLeft className="h-3 w-3" />
                            {t.label}
                          </div>
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  {changeStatus.error && (
                    <p className="text-xs text-red-600">{changeStatus.error}</p>
                  )}
                </div>
              )}

              {/* Assign staff */}
              {canManage && agents && agents.length > 0 && (
                <div className="space-y-2">
                  <p className="text-xs text-muted-foreground">Assign Staff</p>
                  <Select
                    onValueChange={handleAssign}
                    disabled={assignAgent.isLoading}
                    value=""
                  >
                    <SelectTrigger className="rounded-xl h-10">
                      <SelectValue placeholder={assignAgent.isLoading ? 'Assigning...' : 'Select agent'} />
                    </SelectTrigger>
                    <SelectContent className="rounded-xl">
                      {agents.map(a => (
                        <SelectItem key={a.id} value={a.id}>
                          <div className="flex items-center gap-2">
                            <UserCheck className="h-3 w-3" />
                            {a.name}
                          </div>
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  {assignAgent.error && (
                    <p className="text-xs text-red-600">{assignAgent.error}</p>
                  )}
                </div>
              )}

              {/* Success feedback */}
              {(statusSuccess || assignSuccess) && (
                <div className="flex items-center gap-2 rounded-xl bg-emerald-50 dark:bg-emerald-950/20 border border-emerald-200 dark:border-emerald-900/50 px-3 py-2">
                  <CheckCircle2 className="h-3.5 w-3.5 text-emerald-600 shrink-0" />
                  <p className="text-xs text-emerald-700 dark:text-emerald-400">{statusSuccess || assignSuccess}</p>
                </div>
              )}
            </div>
          )}

          {/* Final status message */}
          {isFinalStatus && (
            <div className="rounded-2xl border bg-muted/30 p-6">
              <p className="text-sm text-muted-foreground text-center">
                This case is {incident.status.toLowerCase()}. No further actions available.
              </p>
            </div>
          )}
        </motion.div>
      </div>
    </div>
  );
}
