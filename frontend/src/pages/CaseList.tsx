import { useState, useMemo } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Search, Filter, Plus, ChevronRight, ChevronLeft } from 'lucide-react';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { StatusBadge } from '@/components/shared/StatusBadge';
import { UrgencyBadge } from '@/components/shared/UrgencyBadge';
import { CategoryBadge } from '@/components/shared/CategoryBadge';
import { EmptyState } from '@/components/shared/EmptyState';
import { PageLoader } from '@/components/shared/PageLoader';
import { ErrorAlert } from '@/components/shared/ErrorAlert';
import { MockBanner } from '@/components/shared/MockBanner';
import { useApi } from '@/hooks/useApi';
import { incidentApi } from '@/api/endpoints';
import type { Page } from '@/api/endpoints';
import { mapIncident } from '@/api/mappers';
import { incidents as mockIncidents } from '@/data/mock';
import type { Incident, IncidentStatus } from '@/types';

const PAGE_SIZE = 20;
const statusFilters: { value: IncidentStatus | 'ALL'; label: string }[] = [
  { value: 'ALL', label: 'All' },
  { value: 'NEW', label: 'New' },
  { value: 'ASSIGNED', label: 'Assigned' },
  { value: 'IN_PROGRESS', label: 'In Progress' },
  { value: 'RESOLVED', label: 'Resolved' },
  { value: 'CLOSED', label: 'Closed' },
  { value: 'CANCELLED', label: 'Cancelled' },
];

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' });
}

export function CaseList() {
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<IncidentStatus | 'ALL'>('ALL');
  const [page, setPage] = useState(0);

  const {
    data: pageData,
    isLoading,
    error,
    isUsingMock,
    refetch,
  } = useApi<{ items: Incident[]; totalPages: number; totalElements: number; currentPage: number }>(
    async () => {
      const params = {
        ...(statusFilter !== 'ALL' ? { status: statusFilter } : {}),
        size: PAGE_SIZE,
        page,
      };
      const res = await incidentApi.list(params);
      return {
        items: res.content.map(mapIncident),
        totalPages: res.totalPages,
        totalElements: res.totalElements,
        currentPage: res.number,
      };
    },
    [statusFilter, page],
    {
      mockData: {
        items: mockIncidents,
        totalPages: 1,
        totalElements: mockIncidents.length,
        currentPage: 0,
      },
    },
  );

  const safeData = pageData ?? { items: mockIncidents, totalPages: 1, totalElements: mockIncidents.length, currentPage: 0 };

  const filtered = useMemo(() => {
    let list = safeData.items;
    if (isUsingMock && statusFilter !== 'ALL') {
      list = list.filter((i) => i.status === statusFilter);
    }
    if (search) {
      const q = search.toLowerCase();
      list = list.filter(
        (i) =>
          i.title.toLowerCase().includes(q) ||
          i.id.toLowerCase().includes(q) ||
          i.createdBy.name.toLowerCase().includes(q),
      );
    }
    return list;
  }, [safeData.items, search, statusFilter, isUsingMock]);

  const handleStatusChange = (s: IncidentStatus | 'ALL') => {
    setStatusFilter(s);
    setPage(0);
  };

  if (isLoading) return <PageLoader message="Loading cases..." />;
  if (error && !pageData) return <ErrorAlert message={error} onRetry={refetch} />;

  return (
    <div className="space-y-6">
      {isUsingMock && <MockBanner />}

      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-end justify-between gap-4">
        <div>
          <h2 className="text-2xl font-semibold tracking-tight">Cases</h2>
          <p className="text-sm text-muted-foreground mt-1">{safeData.totalElements} total cases</p>
        </div>
        <Link to="/cases/new" className="shrink-0">
          <Button className="rounded-xl w-full sm:w-auto">
            <Plus className="mr-2 h-4 w-4" />
            New Case
          </Button>
        </Link>
      </div>

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Search cases by title, ID, or resident..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            className="pl-9 h-10 rounded-xl"
          />
        </div>
        <div className="flex gap-1.5 overflow-x-auto pb-1">
          {statusFilters.map(s => (
            <button
              key={s.value}
              onClick={() => handleStatusChange(s.value)}
              className={`shrink-0 px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                statusFilter === s.value
                  ? 'bg-foreground text-background'
                  : 'bg-muted text-muted-foreground hover:text-foreground'
              }`}
            >
              {s.label}
            </button>
          ))}
        </div>
      </div>

      {/* Table */}
      {filtered.length === 0 ? (
        <EmptyState icon={Filter} title="No cases found" description="Try adjusting your search or filters." />
      ) : (
        <div className="rounded-2xl border bg-card overflow-hidden">
          {/* Table header */}
          <div className="hidden md:grid grid-cols-[1fr_120px_100px_120px_140px_40px] gap-4 px-6 py-3 border-b bg-muted/30 text-xs font-medium text-muted-foreground">
            <span>Case</span>
            <span>Category</span>
            <span>Urgency</span>
            <span>Status</span>
            <span>Created</span>
            <span />
          </div>

          {/* Rows */}
          <div className="divide-y">
            {filtered.map((incident, i) => (
              <motion.div
                key={incident.id}
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ delay: i * 0.02 }}
              >
                <Link
                  to={`/cases/${incident.id}`}
                  className="grid grid-cols-1 md:grid-cols-[1fr_120px_100px_120px_140px_40px] gap-2 md:gap-4 px-6 py-4 hover:bg-accent/30 transition-colors items-center"
                >
                  <div className="min-w-0">
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-mono text-muted-foreground">{incident.id}</span>
                    </div>
                    <p className="text-sm font-medium truncate mt-0.5">{incident.title}</p>
                    <p className="text-xs text-muted-foreground mt-0.5 md:hidden">
                      {incident.createdBy.name} · {formatDate(incident.createdAt)}
                    </p>
                  </div>
                  <div className="hidden md:block"><CategoryBadge category={incident.category} /></div>
                  <div className="hidden md:block"><UrgencyBadge priority={incident.priority} /></div>
                  <div className="hidden md:block"><StatusBadge status={incident.status} /></div>
                  <div className="hidden md:block">
                    <p className="text-xs text-muted-foreground">{formatDate(incident.createdAt)}</p>
                    <p className="text-xs text-muted-foreground/60">{incident.createdBy.name}</p>
                  </div>
                  <div className="hidden md:flex justify-end">
                    <ChevronRight className="h-4 w-4 text-muted-foreground" />
                  </div>

                  {/* Mobile badges */}
                  <div className="flex gap-2 md:hidden">
                    <StatusBadge status={incident.status} />
                    <UrgencyBadge priority={incident.priority} />
                    <CategoryBadge category={incident.category} />
                  </div>
                </Link>
              </motion.div>
            ))}
          </div>

          {/* Pagination */}
          {safeData.totalPages > 1 && !isUsingMock && (
            <div className="flex items-center justify-between px-6 py-3 border-t bg-muted/30">
              <p className="text-xs text-muted-foreground">
                Page {safeData.currentPage + 1} of {safeData.totalPages}
              </p>
              <div className="flex gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  className="rounded-lg h-8"
                  onClick={() => setPage(p => Math.max(0, p - 1))}
                  disabled={safeData.currentPage === 0}
                >
                  <ChevronLeft className="h-3 w-3 mr-1" />
                  Previous
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  className="rounded-lg h-8"
                  onClick={() => setPage(p => p + 1)}
                  disabled={safeData.currentPage >= safeData.totalPages - 1}
                >
                  Next
                  <ChevronRight className="h-3 w-3 ml-1" />
                </Button>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
