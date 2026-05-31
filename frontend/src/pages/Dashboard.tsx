import { motion } from 'framer-motion';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, AreaChart, Area,
} from 'recharts';
import {
  ClipboardList, AlertTriangle, CheckCircle2, Clock,
  TrendingUp, ArrowRight,
} from 'lucide-react';
import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { StatCard } from '@/components/shared/StatCard';
import { StatusBadge } from '@/components/shared/StatusBadge';
import { UrgencyBadge } from '@/components/shared/UrgencyBadge';
import { PageLoader } from '@/components/shared/PageLoader';
import { ErrorAlert } from '@/components/shared/ErrorAlert';
import { MockBanner } from '@/components/shared/MockBanner';
import { useAuth } from '@/contexts/AuthContext';
import { useApi } from '@/hooks/useApi';
import { STATUS_LABELS } from '@/lib/constants';
import { formatRelativeTime } from '@/lib/utils';
import { dashboardApi, incidentApi, type WeeklyTrendResponse } from '@/api/endpoints';
import { mapDashboardSummary, mapIncident } from '@/api/mappers';
import {
  dashboardSummary as mockSummary,
  statusCounts as mockStatusCounts,
  categoryCounts as mockCategoryCounts,
  priorityCounts as mockPriorityCounts,
  weeklyTrend as mockWeeklyTrend,
  incidents as mockIncidents,
} from '@/data/mock';
import type { DashboardSummary, StatusCount, PriorityCount, CategoryCount, Incident, Priority } from '@/types';

const pieColors = ['#6366f1', '#f59e0b', '#8b5cf6', '#10b981', '#94a3b8', '#ef4444'];
const categoryColors = ['#6366f1', '#ef4444', '#f59e0b', '#3b82f6', '#10b981', '#8b5cf6', '#06b6d4', '#ec4899', '#94a3b8'];

export function Dashboard() {
  const { user } = useAuth();
  const firstName = user?.name?.split(' ')[0] ?? 'there';

  const {
    data: summary,
    isLoading: summaryLoading,
    error: summaryError,
    isUsingMock: summaryMock,
    refetch: refetchSummary,
  } = useApi<DashboardSummary>(
    async () => {
      const res = await dashboardApi.summary();
      return mapDashboardSummary(res);
    },
    [],
    { mockData: mockSummary },
  );

  const { data: statusCounts } = useApi<StatusCount[]>(
    async () => {
      const res = await dashboardApi.byStatus();
      return res.map((s) => ({ status: s.status as StatusCount['status'], count: s.count }));
    },
    [],
    { mockData: mockStatusCounts },
  );

  const { data: priorityCounts } = useApi<PriorityCount[]>(
    async () => {
      const res = await dashboardApi.bySeverity();
      return res.map((p) => ({ priority: p.priority as PriorityCount['priority'], count: p.count }));
    },
    [],
    { mockData: mockPriorityCounts },
  );

  const { data: categoryCounts } = useApi<CategoryCount[]>(
    async () => {
      const res = await dashboardApi.byCategory();
      return res.map((c) => ({ category: c.category as CategoryCount['category'], count: c.count }));
    },
    [],
    { mockData: mockCategoryCounts },
  );

  const { data: recentIncidents } = useApi<Incident[]>(
    async () => {
      const res = await dashboardApi.recentActivity();
      return res.map(mapIncident);
    },
    [],
    { mockData: mockIncidents.slice(0, 10) },
  );

  const { data: criticalIncidents } = useApi<Incident[]>(
    async () => {
      const res = await incidentApi.list({ priority: 'CRITICAL' as Priority, size: 10 });
      return res.content.map(mapIncident).filter(i => i.status !== 'CLOSED' && i.status !== 'RESOLVED');
    },
    [],
    { mockData: mockIncidents.filter(i => i.priority === 'CRITICAL' && i.status !== 'CLOSED' && i.status !== 'RESOLVED') },
  );

  const { data: weeklyTrendData } = useApi<WeeklyTrendResponse[]>(
    async () => dashboardApi.weeklyTrend(),
    [],
    { mockData: mockWeeklyTrend },
  );

  const isUsingMock = summaryMock;
  const weeklyTrend = weeklyTrendData ?? mockWeeklyTrend;

  if (summaryLoading) return <PageLoader message="Loading dashboard..." />;
  if (summaryError && !summary) return <ErrorAlert message={summaryError} onRetry={refetchSummary} />;
  if (!summary) return null;

  const safeStatusCounts = statusCounts ?? mockStatusCounts;
  const safePriorityCounts = priorityCounts ?? mockPriorityCounts;
  const safeCategoryCounts = categoryCounts ?? mockCategoryCounts;
  const safeCriticalIncidents = criticalIncidents ?? mockIncidents.filter(i => i.priority === 'CRITICAL' && i.status !== 'CLOSED' && i.status !== 'RESOLVED');
  const safeRecentIncidents = recentIncidents ?? mockIncidents.slice(0, 10);

  const hour = new Date().getHours();
  const greeting = hour < 12 ? 'Good morning' : hour < 18 ? 'Good afternoon' : 'Good evening';

  return (
    <div className="space-y-8">
      {isUsingMock && <MockBanner />}

      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-end justify-between gap-4">
        <div>
          <motion.h2
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            className="text-2xl font-semibold tracking-tight"
          >
            {greeting}, {firstName}
          </motion.h2>
          <p className="text-sm text-muted-foreground mt-1">Here&apos;s what&apos;s happening across your properties today.</p>
        </div>
        <Link to="/cases/new" className="shrink-0">
          <Button className="rounded-xl w-full sm:w-auto">
            New Case
            <ArrowRight className="ml-2 h-4 w-4" />
          </Button>
        </Link>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard title="Total Cases" value={summary.totalCases} icon={ClipboardList} />
        <StatCard title="Open Cases" value={summary.openCases} icon={AlertTriangle} accent="text-amber-600" subtitle="Requires attention" />
        <StatCard title="Resolved" value={summary.resolvedToday} icon={CheckCircle2} accent="text-emerald-600" subtitle="Resolved & closed" />
        <StatCard title="Critical Open" value={summary.criticalOpen} icon={Clock} accent="text-red-600" />
      </div>

      {/* Charts row */}
      <div className="grid lg:grid-cols-3 gap-6">
        {/* Weekly trend */}
        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
          className="lg:col-span-2 rounded-2xl border bg-card p-6"
        >
          <div className="flex items-center justify-between mb-6">
            <div>
              <h3 className="text-sm font-semibold">Weekly Trend</h3>
              <p className="text-xs text-muted-foreground mt-0.5">Cases created vs resolved this week</p>
            </div>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </div>
          <ResponsiveContainer width="100%" height={240}>
            <AreaChart data={weeklyTrend}>
              <defs>
                <linearGradient id="created" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#6366f1" stopOpacity={0.15} />
                  <stop offset="95%" stopColor="#6366f1" stopOpacity={0} />
                </linearGradient>
                <linearGradient id="resolved" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#10b981" stopOpacity={0.15} />
                  <stop offset="95%" stopColor="#10b981" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
              <XAxis dataKey="day" tick={{ fontSize: 12 }} stroke="var(--color-muted-foreground)" />
              <YAxis tick={{ fontSize: 12 }} stroke="var(--color-muted-foreground)" />
              <Tooltip contentStyle={{ borderRadius: 12, border: '1px solid var(--color-border)', fontSize: 12 }} />
              <Area type="monotone" dataKey="created" stroke="#6366f1" strokeWidth={2} fill="url(#created)" />
              <Area type="monotone" dataKey="resolved" stroke="#10b981" strokeWidth={2} fill="url(#resolved)" />
            </AreaChart>
          </ResponsiveContainer>
        </motion.div>

        {/* Status breakdown */}
        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3 }}
          className="rounded-2xl border bg-card p-6"
        >
          <h3 className="text-sm font-semibold mb-1">By Status</h3>
          <p className="text-xs text-muted-foreground mb-4">Current distribution</p>
          <ResponsiveContainer width="100%" height={180}>
            <PieChart>
              <Pie data={safeStatusCounts} dataKey="count" nameKey="status" cx="50%" cy="50%" innerRadius={50} outerRadius={75} paddingAngle={2}>
                {safeStatusCounts.map((_, i) => <Cell key={i} fill={pieColors[i % pieColors.length]} />)}
              </Pie>
              <Tooltip contentStyle={{ borderRadius: 12, fontSize: 12, border: '1px solid var(--color-border)' }} />
            </PieChart>
          </ResponsiveContainer>
          <div className="grid grid-cols-2 gap-x-4 gap-y-1.5 mt-2">
            {safeStatusCounts.map((s, i) => (
              <div key={s.status} className="flex items-center gap-2 text-xs">
                <div className="h-2 w-2 rounded-full" style={{ backgroundColor: pieColors[i % pieColors.length] }} />
                <span className="text-muted-foreground">{STATUS_LABELS[s.status] ?? s.status}</span>
                <span className="ml-auto font-medium">{s.count}</span>
              </div>
            ))}
          </div>
        </motion.div>
      </div>

      {/* Bottom row */}
      <div className="grid lg:grid-cols-3 gap-6">
        {/* Category chart */}
        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.35 }}
          className="rounded-2xl border bg-card p-6"
        >
          <h3 className="text-sm font-semibold mb-1">By Category</h3>
          <p className="text-xs text-muted-foreground mb-4">All time</p>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={safeCategoryCounts} layout="vertical" margin={{ left: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" horizontal={false} />
              <XAxis type="number" tick={{ fontSize: 11 }} stroke="var(--color-muted-foreground)" />
              <YAxis dataKey="category" type="category" tick={{ fontSize: 11 }} stroke="var(--color-muted-foreground)" width={90} />
              <Tooltip contentStyle={{ borderRadius: 12, fontSize: 12, border: '1px solid var(--color-border)' }} />
              <Bar dataKey="count" radius={[0, 4, 4, 0]}>
                {safeCategoryCounts.map((_, i) => <Cell key={i} fill={categoryColors[i % categoryColors.length]} />)}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </motion.div>

        {/* Urgency breakdown */}
        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.4 }}
          className="rounded-2xl border bg-card p-6"
        >
          <h3 className="text-sm font-semibold mb-1">By Urgency</h3>
          <p className="text-xs text-muted-foreground mb-6">Open cases</p>
          <div className="space-y-4">
            {safePriorityCounts.map(p => {
              const total = safePriorityCounts.reduce((s, x) => s + x.count, 0);
              const pct = total > 0 ? Math.round((p.count / total) * 100) : 0;
              return (
                <div key={p.priority} className="space-y-2">
                  <div className="flex items-center justify-between">
                    <UrgencyBadge priority={p.priority} />
                    <span className="text-sm font-medium">{p.count}</span>
                  </div>
                  <div className="h-1.5 rounded-full bg-muted overflow-hidden">
                    <motion.div
                      initial={{ width: 0 }}
                      animate={{ width: `${pct}%` }}
                      transition={{ delay: 0.5, duration: 0.6, ease: 'easeOut' }}
                      className="h-full rounded-full"
                      style={{ backgroundColor: p.priority === 'CRITICAL' ? '#ef4444' : p.priority === 'HIGH' ? '#f97316' : p.priority === 'MEDIUM' ? '#eab308' : '#0ea5e9' }}
                    />
                  </div>
                </div>
              );
            })}
          </div>
        </motion.div>

        {/* Recent activity */}
        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.45 }}
          className="rounded-2xl border bg-card p-6"
        >
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-sm font-semibold">Recent Activity</h3>
            <Link to="/cases" className="text-xs text-muted-foreground hover:text-foreground">View all</Link>
          </div>
          <div className="space-y-3">
            {safeRecentIncidents.slice(0, 5).map(inc => (
              <Link key={inc.id} to={`/cases/${inc.id}`} className="flex gap-3 hover:bg-accent/30 rounded-lg p-1.5 -mx-1.5 transition-colors">
                <div className="h-8 w-8 shrink-0 rounded-full bg-muted flex items-center justify-center">
                  <span className="text-[10px] font-medium">{inc.createdBy.name.split(' ').map(n => n[0]).join('')}</span>
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-xs leading-snug truncate">{inc.title}</p>
                  <p className="text-[10px] text-muted-foreground mt-0.5">{inc.createdBy.name} · {formatRelativeTime(inc.updatedAt)}</p>
                </div>
              </Link>
            ))}
          </div>
        </motion.div>
      </div>

      {/* Critical cases */}
      {safeCriticalIncidents.length > 0 && (
        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.5 }}
          className="rounded-2xl border border-red-200 dark:border-red-900/50 bg-red-50/50 dark:bg-red-950/20 p-6"
        >
          <div className="flex items-center gap-2 mb-4">
            <AlertTriangle className="h-4 w-4 text-red-600" />
            <h3 className="text-sm font-semibold text-red-700 dark:text-red-400">Critical Cases Requiring Attention</h3>
          </div>
          <div className="space-y-3">
            {safeCriticalIncidents.map(c => (
              <Link key={c.id} to={`/cases/${c.id}`} className="flex items-center gap-4 rounded-xl bg-white/60 dark:bg-white/5 p-3 hover:bg-white dark:hover:bg-white/10 transition-colors">
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium truncate">{c.title}</p>
                  <p className="text-xs text-muted-foreground mt-0.5">{c.id} · Reported by {c.createdBy.name}</p>
                </div>
                <StatusBadge status={c.status} />
              </Link>
            ))}
          </div>
        </motion.div>
      )}
    </div>
  );
}
