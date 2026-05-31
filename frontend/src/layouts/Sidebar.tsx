import { NavLink, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard, ClipboardList, PlusCircle,
  Settings, LogOut, Building2,
} from 'lucide-react';
import { cn, getInitials } from '@/lib/utils';
import { ROLE_LABELS } from '@/lib/constants';
import { useAuth } from '@/contexts/AuthContext';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Separator } from '@/components/ui/separator';

const navItems = [
  { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/cases', icon: ClipboardList, label: 'Cases' },
  { to: '/cases/new', icon: PlusCircle, label: 'New Case' },
  { to: '/settings', icon: Settings, label: 'Settings' },
];

export function Sidebar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <aside className="hidden lg:flex flex-col w-[260px] shrink-0 border-r bg-card/50 backdrop-blur-xl">
      {/* Logo */}
      <div className="flex items-center gap-3 px-6 h-16 border-b">
        <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-foreground">
          <Building2 className="h-4 w-4 text-background" />
        </div>
        <div>
          <p className="text-sm font-semibold tracking-tight">ResolveHub</p>
          <p className="text-[10px] text-muted-foreground uppercase tracking-wider">Operations</p>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 px-3 py-4 space-y-1">
        {navItems.map(item => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              cn(
                'flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-colors',
                isActive
                  ? 'bg-accent text-accent-foreground'
                  : 'text-muted-foreground hover:text-foreground hover:bg-accent/50'
              )
            }
          >
            <item.icon className="h-4 w-4" />
            {item.label}
          </NavLink>
        ))}
      </nav>

      <Separator />

      {/* User */}
      <div className="p-4">
        <div className="flex items-center gap-3">
          <Avatar className="h-9 w-9">
            <AvatarFallback className="text-xs bg-muted">
              {user ? getInitials(user.name) : '??'}
            </AvatarFallback>
          </Avatar>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium truncate">{user?.name ?? 'Unknown'}</p>
            <p className="text-xs text-muted-foreground capitalize">
              {user ? (ROLE_LABELS[user.role] ?? user.role.toLowerCase()) : ''}
            </p>
          </div>
          <button
            onClick={handleLogout}
            className="rounded-lg p-2 text-muted-foreground hover:text-foreground hover:bg-accent transition-colors"
            title="Sign out"
          >
            <LogOut className="h-4 w-4" />
          </button>
        </div>
      </div>
    </aside>
  );
}
