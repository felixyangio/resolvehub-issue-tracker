import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Button } from '@/components/ui/button';
import { Separator } from '@/components/ui/separator';
import { Badge } from '@/components/ui/badge';
import { useAuth } from '@/contexts/AuthContext';
import { ROLE_LABELS } from '@/lib/constants';
import { getInitials } from '@/lib/utils';
import { useTheme } from '@/hooks/useTheme';
import { Shield, LogOut, Sun, Moon, Monitor } from 'lucide-react';

const themeOptions = [
  { value: 'light' as const, label: 'Light', icon: Sun },
  { value: 'dark' as const, label: 'Dark', icon: Moon },
  { value: 'system' as const, label: 'System', icon: Monitor },
];

export function Settings() {
  const { user, logout } = useAuth();
  const { theme, setTheme } = useTheme();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (!user) return null;

  return (
    <div className="max-w-2xl space-y-8">
      <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }}>
        <h2 className="text-2xl font-semibold tracking-tight">Settings</h2>
        <p className="text-sm text-muted-foreground mt-1">View your profile information</p>
      </motion.div>

      {/* Profile */}
      <motion.div
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
        className="rounded-2xl border bg-card p-6 space-y-6"
      >
        <div className="flex items-center gap-4">
          <Avatar className="h-16 w-16">
            <AvatarFallback className="text-lg bg-muted">{getInitials(user.name)}</AvatarFallback>
          </Avatar>
          <div>
            <h3 className="font-semibold">{user.name}</h3>
            <p className="text-sm text-muted-foreground">{user.email}</p>
            <Badge variant="secondary" className="mt-1.5 rounded-full text-xs">
              <Shield className="h-3 w-3 mr-1" />
              {ROLE_LABELS[user.role] ?? user.role}
            </Badge>
          </div>
        </div>

        <Separator />

        <div className="grid gap-4">
          <div className="grid sm:grid-cols-2 gap-4">
            <div className="space-y-1">
              <p className="text-xs text-muted-foreground">Full Name</p>
              <p className="text-sm font-medium">{user.name}</p>
            </div>
            <div className="space-y-1">
              <p className="text-xs text-muted-foreground">Email</p>
              <p className="text-sm font-medium">{user.email}</p>
            </div>
          </div>
          <div className="grid sm:grid-cols-2 gap-4">
            <div className="space-y-1">
              <p className="text-xs text-muted-foreground">Role</p>
              <p className="text-sm font-medium">{ROLE_LABELS[user.role] ?? user.role}</p>
            </div>
            <div className="space-y-1">
              <p className="text-xs text-muted-foreground">Account ID</p>
              <p className="text-sm font-mono text-muted-foreground">{user.id}</p>
            </div>
          </div>
        </div>
      </motion.div>

      {/* Appearance */}
      <motion.div
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.15 }}
        className="rounded-2xl border bg-card p-6 space-y-4"
      >
        <div>
          <h3 className="font-semibold">Appearance</h3>
          <p className="text-sm text-muted-foreground mt-1">Choose your preferred theme</p>
        </div>
        <div className="flex gap-2">
          {themeOptions.map(opt => (
            <button
              key={opt.value}
              onClick={() => setTheme(opt.value)}
              className={`flex items-center gap-2 rounded-xl px-4 py-2.5 text-sm font-medium transition-colors ${
                theme === opt.value
                  ? 'bg-foreground text-background'
                  : 'bg-muted text-muted-foreground hover:text-foreground'
              }`}
            >
              <opt.icon className="h-4 w-4" />
              {opt.label}
            </button>
          ))}
        </div>
      </motion.div>

      {/* Sign out */}
      <motion.div
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2 }}
        className="rounded-2xl border border-red-200 dark:border-red-900/50 bg-red-50/30 dark:bg-red-950/10 p-6"
      >
        <h3 className="font-semibold text-red-700 dark:text-red-400">Sign Out</h3>
        <p className="text-sm text-muted-foreground mt-1">Sign out of your account on this device.</p>
        <Button
          variant="outline"
          className="mt-4 rounded-xl border-red-200 text-red-600 hover:bg-red-50 dark:border-red-900 dark:hover:bg-red-950/30"
          onClick={handleLogout}
        >
          <LogOut className="mr-2 h-4 w-4" />
          Sign Out
        </Button>
      </motion.div>
    </div>
  );
}
