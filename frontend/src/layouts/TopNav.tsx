import { useLocation, useNavigate, Link, NavLink } from 'react-router-dom';
import { useState } from 'react';
import { Bell, Menu, Building2, LayoutDashboard, ClipboardList, PlusCircle, Settings, X, LogOut } from 'lucide-react';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Separator } from '@/components/ui/separator';
import { Button } from '@/components/ui/button';
import { useAuth } from '@/contexts/AuthContext';
import { cn, getInitials } from '@/lib/utils';
import { ROLE_LABELS } from '@/lib/constants';

const pageTitles: Record<string, string> = {
  '/dashboard': 'Dashboard',
  '/cases': 'Cases',
  '/cases/new': 'New Case',
  '/settings': 'Settings',
};

const mobileNav = [
  { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/cases', icon: ClipboardList, label: 'Cases' },
  { to: '/cases/new', icon: PlusCircle, label: 'New Case' },
  { to: '/settings', icon: Settings, label: 'Settings' },
];

export function TopNav() {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const title = pageTitles[location.pathname] || (location.pathname.startsWith('/cases/') ? 'Case Detail' : 'ResolveHub');
  const [mobileOpen, setMobileOpen] = useState(false);

  return (
    <>
      <header className="sticky top-0 z-30 flex h-16 items-center gap-4 border-b bg-background/80 backdrop-blur-xl px-6">
        <Button variant="ghost" size="icon" className="lg:hidden" onClick={() => setMobileOpen(true)}>
          <Menu className="h-5 w-5" />
        </Button>

        <h1 className="text-lg font-semibold tracking-tight">{title}</h1>

        <div className="ml-auto flex items-center gap-2">
          <Button variant="ghost" size="icon" title="Notifications (coming soon)" disabled className="opacity-50">
            <Bell className="h-4 w-4" />
          </Button>
          <Link to="/settings">
            <Avatar className="h-8 w-8 cursor-pointer">
              <AvatarFallback className="text-xs bg-muted">
                {user ? getInitials(user.name) : '??'}
              </AvatarFallback>
            </Avatar>
          </Link>
        </div>
      </header>

      {/* Mobile drawer */}
      {mobileOpen && (
        <>
          <div className="fixed inset-0 z-40 bg-black/40 lg:hidden" onClick={() => setMobileOpen(false)} />
          <div className="fixed inset-y-0 left-0 z-50 w-[280px] bg-background border-r p-0 lg:hidden flex flex-col">
            <div className="flex items-center gap-3 px-6 h-16 border-b">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-foreground">
                <Building2 className="h-4 w-4 text-background" />
              </div>
              <p className="text-sm font-semibold">ResolveHub</p>
              <button className="ml-auto rounded-lg p-2 hover:bg-accent" onClick={() => setMobileOpen(false)}>
                <X className="h-4 w-4" />
              </button>
            </div>
            <nav className="flex-1 px-3 py-4 space-y-1">
              {mobileNav.map(item => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  onClick={() => setMobileOpen(false)}
                  className={({ isActive }) =>
                    cn(
                      'flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-colors',
                      isActive ? 'bg-accent text-accent-foreground' : 'text-muted-foreground hover:bg-accent/50'
                    )
                  }
                >
                  <item.icon className="h-4 w-4" />
                  {item.label}
                </NavLink>
              ))}
            </nav>
            <Separator />
            <div className="p-4">
              <div className="flex items-center gap-3">
                <Avatar className="h-9 w-9">
                  <AvatarFallback className="text-xs bg-muted">
                    {user ? getInitials(user.name) : '??'}
                  </AvatarFallback>
                </Avatar>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium truncate">{user?.name ?? 'Unknown'}</p>
                  <p className="text-xs text-muted-foreground">
                    {user ? (ROLE_LABELS[user.role] ?? user.role.toLowerCase()) : ''}
                  </p>
                </div>
                <button
                  onClick={() => { logout(); navigate('/login'); setMobileOpen(false); }}
                  className="rounded-lg p-2 text-muted-foreground hover:text-foreground hover:bg-accent transition-colors"
                  title="Sign out"
                >
                  <LogOut className="h-4 w-4" />
                </button>
              </div>
            </div>
          </div>
        </>
      )}
    </>
  );
}
