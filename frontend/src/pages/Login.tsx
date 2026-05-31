import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Building2, ArrowLeft, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useAuth } from '@/contexts/AuthContext';
import { ApiError } from '@/api/client';

export function Login() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, loginWithMock, isLoading } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);

  const from = (location.state as { from?: { pathname: string } })?.from?.pathname ?? '/dashboard';

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    try {
      await login(email, password);
      navigate(from, { replace: true });
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.status === 429) {
          setError(err.message || 'Too many login attempts. Please wait a few minutes and try again.');
        } else {
          setError('Invalid email or password. Please try again.');
        }
      } else {
        setError('Cannot reach the server. Please check the backend is running, or use a demo account below.');
      }
    }
  };

  const handleDemoLogin = async (demoEmail: string) => {
    setEmail(demoEmail);
    setPassword('password');
    setError(null);
    try {
      await login(demoEmail, 'password');
      navigate(from, { replace: true });
    } catch (err) {
      if (err instanceof ApiError) {
        setError('Demo login failed. Make sure demo users are seeded in the database.');
      } else {
        loginWithMock(demoEmail);
        navigate(from, { replace: true });
      }
    }
  };

  return (
    <div className="min-h-screen flex">
      {/* Left panel */}
      <div className="hidden lg:flex lg:w-1/2 relative bg-foreground text-background items-center justify-center p-12">
        <div className="absolute inset-0 overflow-hidden">
          <div className="absolute top-1/3 left-1/3 h-[400px] w-[400px] rounded-full bg-white/5 blur-3xl" />
          <div className="absolute bottom-1/4 right-1/4 h-[300px] w-[300px] rounded-full bg-white/3 blur-3xl" />
        </div>
        <div className="relative max-w-md space-y-6">
          <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-white/10">
            <Building2 className="h-6 w-6" />
          </div>
          <h2 className="text-3xl font-semibold tracking-tight leading-tight">
            Manage every resident case from report to resolution
          </h2>
          <p className="text-white/60 leading-relaxed">
            ResolveHub gives your property team a single platform for repair requests, complaints, and operational insights. No more lost emails or missed follow-ups.
          </p>
          <div className="flex items-center gap-6 pt-4">
            <div>
              <p className="text-2xl font-semibold">6</p>
              <p className="text-xs text-white/50">Status stages</p>
            </div>
            <div className="h-8 w-px bg-white/10" />
            <div>
              <p className="text-2xl font-semibold">4</p>
              <p className="text-xs text-white/50">User roles</p>
            </div>
            <div className="h-8 w-px bg-white/10" />
            <div>
              <p className="text-2xl font-semibold">9</p>
              <p className="text-xs text-white/50">Case categories</p>
            </div>
          </div>
        </div>
      </div>

      {/* Right panel */}
      <div className="flex-1 flex items-center justify-center p-6">
        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4 }}
          className="w-full max-w-sm space-y-8"
        >
          <div>
            <Link to="/" className="inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground transition-colors mb-8">
              <ArrowLeft className="h-4 w-4" />
              Back
            </Link>
            <div className="flex items-center gap-3 mb-2 lg:hidden">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-foreground">
                <Building2 className="h-4 w-4 text-background" />
              </div>
              <span className="text-sm font-semibold">ResolveHub</span>
            </div>
            <h1 className="text-2xl font-semibold tracking-tight">Welcome back</h1>
            <p className="text-sm text-muted-foreground mt-1">Sign in to your account to continue</p>
          </div>

          {error && (
            <div className="rounded-xl border border-red-200 bg-red-50 dark:border-red-900/50 dark:bg-red-950/20 px-4 py-3">
              <p className="text-sm text-red-700 dark:text-red-400">{error}</p>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                value={email}
                onChange={e => setEmail(e.target.value)}
                placeholder="you@property.io"
                className="h-11 rounded-xl"
                disabled={isLoading}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">Password</Label>
              <Input
                id="password"
                type="password"
                value={password}
                onChange={e => setPassword(e.target.value)}
                placeholder="Enter your password"
                className="h-11 rounded-xl"
                disabled={isLoading}
                required
              />
            </div>
            <Button type="submit" className="w-full h-11 rounded-xl" disabled={isLoading}>
              {isLoading ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Signing in...
                </>
              ) : (
                'Sign in'
              )}
            </Button>
          </form>

          <p className="text-center text-sm text-muted-foreground">
            Don&apos;t have an account?{' '}
            <Link to="/register" className="text-foreground font-medium hover:underline">
              Create one
            </Link>
          </p>

          <div className="rounded-xl border bg-muted/30 p-4">
            <p className="text-xs font-medium mb-2">Demo Accounts</p>
            <p className="text-[10px] text-muted-foreground mb-3">Click to sign in instantly with demo data</p>
            <div className="space-y-1.5">
              {[
                { email: 'carol@property.io', role: 'Property Manager' },
                { email: 'bob@property.io', role: 'Maintenance Staff' },
                { email: 'alice@tenant.io', role: 'Resident' },
              ].map(acc => (
                <button
                  key={acc.email}
                  type="button"
                  onClick={() => handleDemoLogin(acc.email)}
                  disabled={isLoading}
                  className="flex w-full items-center justify-between rounded-lg px-3 py-2 text-xs hover:bg-accent transition-colors disabled:opacity-50"
                >
                  <span className="font-mono">{acc.email}</span>
                  <span className="text-muted-foreground">{acc.role}</span>
                </button>
              ))}
            </div>
          </div>
        </motion.div>
      </div>
    </div>
  );
}
