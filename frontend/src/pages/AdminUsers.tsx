import { useState } from 'react';
import { motion } from 'framer-motion';
import {
  UserPlus, Trash2, Shield, ChevronLeft, ChevronRight,
  ToggleLeft, ToggleRight, Eye, EyeOff,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Separator } from '@/components/ui/separator';
import { PageLoader } from '@/components/shared/PageLoader';
import { ErrorAlert } from '@/components/shared/ErrorAlert';
import { useApi, useMutation } from '@/hooks/useApi';
import { adminApi, type UserResponse } from '@/api/endpoints';
import type { Page } from '@/api/endpoints';
import { getInitials } from '@/lib/utils';
import { ROLE_LABELS } from '@/lib/constants';
import type { Role } from '@/types';

const ROLES: Role[] = ['USER', 'AGENT', 'MANAGER', 'ADMIN'];

const ROLE_COLORS: Record<Role, string> = {
  USER: 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400',
  AGENT: 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400',
  MANAGER: 'bg-purple-100 text-purple-700 dark:bg-purple-900/30 dark:text-purple-400',
  ADMIN: 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400',
};

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' });
}

export function AdminUsers() {
  const [page, setPage] = useState(0);
  const [showCreate, setShowCreate] = useState(false);
  const [editingRole, setEditingRole] = useState<string | null>(null);

  const {
    data: pageData,
    isLoading,
    error,
    refetch,
  } = useApi<Page<UserResponse>>(
    () => adminApi.listUsers(page),
    [page],
  );

  const createMutation = useMutation(adminApi.createUser);
  const updateMutation = useMutation(adminApi.updateUser);
  const deleteMutation = useMutation(adminApi.deleteUser);

  const handleRoleChange = async (userId: string, role: string) => {
    await updateMutation.execute(userId, { role });
    setEditingRole(null);
    refetch();
  };

  const handleToggleEnabled = async (userId: string, enabled: boolean) => {
    await updateMutation.execute(userId, { enabled: !enabled });
    refetch();
  };

  const handleDelete = async (userId: string, name: string) => {
    if (!confirm(`Delete user "${name}"? This cannot be undone.`)) return;
    await deleteMutation.execute(userId);
    refetch();
  };

  if (isLoading) return <PageLoader message="Loading users..." />;
  if (error && !pageData) return <ErrorAlert message={error} onRetry={refetch} />;

  const users = pageData?.content ?? [];
  const totalPages = pageData?.totalPages ?? 0;
  const totalElements = pageData?.totalElements ?? 0;

  return (
    <div className="space-y-6">
      {/* Header */}
      <motion.div
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        className="flex flex-col sm:flex-row sm:items-end justify-between gap-4"
      >
        <div>
          <h2 className="text-2xl font-semibold tracking-tight">User Management</h2>
          <p className="text-sm text-muted-foreground mt-1">{totalElements} users registered</p>
        </div>
        <Button className="rounded-xl w-full sm:w-auto" onClick={() => setShowCreate(true)}>
          <UserPlus className="mr-2 h-4 w-4" />
          Add User
        </Button>
      </motion.div>

      {/* Create user panel */}
      {showCreate && (
        <motion.div
          initial={{ opacity: 0, y: -8 }}
          animate={{ opacity: 1, y: 0 }}
          className="rounded-2xl border bg-card p-6"
        >
          <CreateUserForm
            onSubmit={async (data) => {
              await createMutation.execute(data);
              setShowCreate(false);
              refetch();
            }}
            onCancel={() => setShowCreate(false)}
            isLoading={createMutation.isLoading}
            error={createMutation.error}
          />
        </motion.div>
      )}

      {/* User table */}
      <motion.div
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.05 }}
        className="rounded-2xl border bg-card overflow-hidden"
      >
        {/* Table header */}
        <div className="hidden md:grid grid-cols-[2fr_2fr_140px_100px_120px_80px] gap-4 px-6 py-3 border-b bg-muted/30 text-xs font-medium text-muted-foreground">
          <span>User</span>
          <span>Email</span>
          <span>Role</span>
          <span>Status</span>
          <span>Joined</span>
          <span />
        </div>

        <div className="divide-y">
          {users.length === 0 ? (
            <div className="px-6 py-12 text-center text-sm text-muted-foreground">No users found.</div>
          ) : users.map((u, i) => (
            <motion.div
              key={u.id}
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: i * 0.03 }}
              className="grid grid-cols-1 md:grid-cols-[2fr_2fr_140px_100px_120px_80px] gap-2 md:gap-4 px-6 py-4 items-center"
            >
              {/* Name */}
              <div className="flex items-center gap-3">
                <Avatar className="h-8 w-8 shrink-0">
                  <AvatarFallback className="text-xs bg-muted">{getInitials(u.name)}</AvatarFallback>
                </Avatar>
                <span className="text-sm font-medium truncate">{u.name}</span>
              </div>

              {/* Email */}
              <span className="text-sm text-muted-foreground truncate">{u.email}</span>

              {/* Role */}
              <div>
                {editingRole === u.id ? (
                  <select
                    autoFocus
                    defaultValue={u.role}
                    onBlur={() => setEditingRole(null)}
                    onChange={e => handleRoleChange(u.id, e.target.value)}
                    className="text-xs rounded-lg border bg-background px-2 py-1 outline-none focus:ring-1 focus:ring-ring"
                  >
                    {ROLES.map(r => (
                      <option key={r} value={r}>{ROLE_LABELS[r] ?? r}</option>
                    ))}
                  </select>
                ) : (
                  <button
                    onClick={() => setEditingRole(u.id)}
                    title="Click to change role"
                    className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-xs font-medium transition-opacity hover:opacity-75 ${ROLE_COLORS[u.role as Role]}`}
                  >
                    <Shield className="h-3 w-3" />
                    {ROLE_LABELS[u.role as Role] ?? u.role}
                  </button>
                )}
              </div>

              {/* Enabled toggle */}
              <button
                onClick={() => handleToggleEnabled(u.id, u.enabled)}
                title={u.enabled ? 'Disable account' : 'Enable account'}
                className="flex items-center gap-1.5 text-xs text-muted-foreground hover:text-foreground transition-colors"
              >
                {u.enabled
                  ? <><ToggleRight className="h-4 w-4 text-green-500" /><span className="hidden md:inline">Active</span></>
                  : <><ToggleLeft className="h-4 w-4 text-muted-foreground" /><span className="hidden md:inline text-muted-foreground/60">Disabled</span></>
                }
              </button>

              {/* Joined date */}
              <span className="hidden md:block text-xs text-muted-foreground">{formatDate(u.createdAt)}</span>

              {/* Delete */}
              <div className="flex justify-end">
                <button
                  onClick={() => handleDelete(u.id, u.name)}
                  title="Delete user"
                  className="rounded-lg p-1.5 text-muted-foreground hover:text-destructive hover:bg-destructive/10 transition-colors"
                >
                  <Trash2 className="h-4 w-4" />
                </button>
              </div>
            </motion.div>
          ))}
        </div>

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="flex items-center justify-between px-6 py-3 border-t bg-muted/30">
            <p className="text-xs text-muted-foreground">Page {page + 1} of {totalPages}</p>
            <div className="flex gap-2">
              <Button variant="outline" size="sm" className="rounded-lg h-8"
                onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0}>
                <ChevronLeft className="h-3 w-3 mr-1" />Previous
              </Button>
              <Button variant="outline" size="sm" className="rounded-lg h-8"
                onClick={() => setPage(p => p + 1)} disabled={page >= totalPages - 1}>
                Next<ChevronRight className="h-3 w-3 ml-1" />
              </Button>
            </div>
          </div>
        )}
      </motion.div>
    </div>
  );
}

// --- Create user inline form ---

interface CreateUserFormProps {
  onSubmit: (data: { name: string; email: string; password: string; role: string }) => Promise<void>;
  onCancel: () => void;
  isLoading: boolean;
  error: string | null;
}

function CreateUserForm({ onSubmit, onCancel, isLoading, error }: CreateUserFormProps) {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState<Role>('USER');
  const [showPassword, setShowPassword] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    await onSubmit({ name, email, password, role });
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="font-semibold">Create New User</h3>
        <button type="button" onClick={onCancel} className="text-xs text-muted-foreground hover:text-foreground">
          Cancel
        </button>
      </div>
      <Separator />

      {error && (
        <p className="text-sm text-destructive bg-destructive/10 rounded-lg px-3 py-2">{error}</p>
      )}

      <div className="grid sm:grid-cols-2 gap-4">
        <div className="space-y-1.5">
          <label className="text-xs font-medium text-muted-foreground">Full Name</label>
          <Input
            placeholder="Jane Smith"
            value={name}
            onChange={e => setName(e.target.value)}
            required
            className="rounded-xl h-9"
          />
        </div>
        <div className="space-y-1.5">
          <label className="text-xs font-medium text-muted-foreground">Email</label>
          <Input
            type="email"
            placeholder="jane@property.io"
            value={email}
            onChange={e => setEmail(e.target.value)}
            required
            className="rounded-xl h-9"
          />
        </div>
        <div className="space-y-1.5">
          <label className="text-xs font-medium text-muted-foreground">Password</label>
          <div className="relative">
            <Input
              type={showPassword ? 'text' : 'password'}
              placeholder="Min. 8 characters"
              value={password}
              onChange={e => setPassword(e.target.value)}
              required
              minLength={8}
              className="rounded-xl h-9 pr-9"
            />
            <button
              type="button"
              onClick={() => setShowPassword(s => !s)}
              className="absolute right-2.5 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
            >
              {showPassword ? <EyeOff className="h-3.5 w-3.5" /> : <Eye className="h-3.5 w-3.5" />}
            </button>
          </div>
        </div>
        <div className="space-y-1.5">
          <label className="text-xs font-medium text-muted-foreground">Role</label>
          <select
            value={role}
            onChange={e => setRole(e.target.value as Role)}
            className="w-full h-9 rounded-xl border bg-background px-3 text-sm outline-none focus:ring-1 focus:ring-ring"
          >
            {ROLES.map(r => (
              <option key={r} value={r}>{ROLE_LABELS[r] ?? r}</option>
            ))}
          </select>
        </div>
      </div>

      <div className="flex justify-end gap-2 pt-1">
        <Button type="button" variant="outline" className="rounded-xl" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit" className="rounded-xl" disabled={isLoading}>
          {isLoading ? 'Creating…' : 'Create User'}
        </Button>
      </div>
    </form>
  );
}
