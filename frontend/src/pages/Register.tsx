import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Building2, ArrowLeft, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { authApi } from '@/api/endpoints';
import { ApiError } from '@/api/client';

export function Register() {
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }
    if (password.length < 8) {
      setError('Password must be at least 8 characters.');
      return;
    }

    setIsLoading(true);
    try {
      await authApi.register(name.trim(), email.trim(), password);
      navigate('/login', { state: { registered: true } });
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.status === 409) {
          setError('An account with this email already exists. Please sign in instead.');
        } else if (err.errors && Object.keys(err.errors).length > 0) {
          // Show the first field-level validation error from the backend
          const firstError = Object.values(err.errors)[0];
          setError(firstError);
        } else {
          setError(err.message || 'Registration failed. Please try again.');
        }
      } else {
        setError('Cannot reach the server. Please try again later.');
      }
    } finally {
      setIsLoading(false);
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
            Join ResolveHub and start reporting issues today
          </h2>
          <p className="text-white/60 leading-relaxed">
            Create your resident account to submit repair requests, track case progress, and communicate directly with your property management team.
          </p>
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
            <Link to="/login" className="inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground transition-colors mb-8">
              <ArrowLeft className="h-4 w-4" />
              Back to Sign In
            </Link>
            <div className="flex items-center gap-3 mb-2 lg:hidden">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-foreground">
                <Building2 className="h-4 w-4 text-background" />
              </div>
              <span className="text-sm font-semibold">ResolveHub</span>
            </div>
            <h1 className="text-2xl font-semibold tracking-tight">Create your account</h1>
            <p className="text-sm text-muted-foreground mt-1">Sign up as a resident to start reporting cases</p>
          </div>

          {error && (
            <div className="rounded-xl border border-red-200 bg-red-50 dark:border-red-900/50 dark:bg-red-950/20 px-4 py-3">
              <p className="text-sm text-red-700 dark:text-red-400">{error}</p>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="name">Full Name</Label>
              <Input
                id="name"
                value={name}
                onChange={e => setName(e.target.value)}
                placeholder="Jane Smith"
                className="h-11 rounded-xl"
                disabled={isLoading}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                value={email}
                onChange={e => setEmail(e.target.value)}
                placeholder="you@example.com"
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
                placeholder="At least 8 characters"
                className="h-11 rounded-xl"
                disabled={isLoading}
                required
                minLength={8}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="confirmPassword">Confirm Password</Label>
              <Input
                id="confirmPassword"
                type="password"
                value={confirmPassword}
                onChange={e => setConfirmPassword(e.target.value)}
                placeholder="Re-enter your password"
                className="h-11 rounded-xl"
                disabled={isLoading}
                required
                minLength={8}
              />
            </div>
            <Button type="submit" className="w-full h-11 rounded-xl" disabled={isLoading}>
              {isLoading ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Creating account...
                </>
              ) : (
                'Create Account'
              )}
            </Button>
          </form>

          <p className="text-center text-sm text-muted-foreground">
            Already have an account?{' '}
            <Link to="/login" className="text-foreground font-medium hover:underline">
              Sign in
            </Link>
          </p>
        </motion.div>
      </div>
    </div>
  );
}
