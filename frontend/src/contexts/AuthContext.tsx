import {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
  type ReactNode,
} from 'react';
import { authApi, type UserResponse } from '@/api/endpoints';
import { ApiError } from '@/api/client';

import type { Role } from '@/types';

export interface AuthUser {
  id: string;
  name: string;
  email: string;
  role: Role;
}

interface AuthContextValue {
  user: AuthUser | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  loginWithMock: (email: string) => void;
  logout: () => void;
  error: string | null;
}

/** Mock users for demo mode when backend is unavailable */
const MOCK_USERS: Record<string, AuthUser> = {
  'carol@property.io': { id: 'u3', name: 'Carol Perry', email: 'carol@property.io', role: 'MANAGER' },
  'bob@property.io': { id: 'u2', name: 'Bob Torres', email: 'bob@property.io', role: 'AGENT' },
  'alice@tenant.io': { id: 'u1', name: 'Alice Chen', email: 'alice@tenant.io', role: 'USER' },
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function mapUserResponse(u: UserResponse): AuthUser {
  return { id: u.id, name: u.name, email: u.email, role: u.role as Role };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(() => {
    const stored = localStorage.getItem('user');
    return stored ? JSON.parse(stored) : null;
  });
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('token'));
  const [isLoading, setIsLoading] = useState(!!token);
  const [error, setError] = useState<string | null>(null);

  // On mount, verify the stored token is still valid
  useEffect(() => {
    if (!token) {
      setIsLoading(false);
      return;
    }
    // Mock tokens start with "mock-" — skip API verification
    if (token.startsWith('mock-')) {
      setIsLoading(false);
      return;
    }
    authApi
      .me()
      .then((u) => {
        const mapped = mapUserResponse(u);
        setUser(mapped);
        localStorage.setItem('user', JSON.stringify(mapped));
      })
      .catch(() => {
        // Token is invalid — clear auth state
        setToken(null);
        setUser(null);
        localStorage.removeItem('token');
        localStorage.removeItem('user');
      })
      .finally(() => setIsLoading(false));
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const login = useCallback(async (email: string, password: string) => {
    setError(null);
    setIsLoading(true);
    try {
      const res = await authApi.login(email, password);
      localStorage.setItem('token', res.token);
      setToken(res.token);

      // Fetch full user profile
      const profile = await authApi.me();
      const mapped = mapUserResponse(profile);
      setUser(mapped);
      localStorage.setItem('user', JSON.stringify(mapped));
    } catch (err) {
      const message =
        err instanceof ApiError ? err.message : 'Login failed. Please try again.';
      setError(message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const loginWithMock = useCallback((email: string) => {
    const mockUser = MOCK_USERS[email] ?? {
      id: 'u0',
      name: email.split('@')[0],
      email,
      role: 'USER',
    };
    const mockToken = `mock-${Date.now()}`;
    localStorage.setItem('token', mockToken);
    localStorage.setItem('user', JSON.stringify(mockUser));
    setToken(mockToken);
    setUser(mockUser);
    setIsLoading(false);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!user && !!token,
        isLoading,
        login,
        loginWithMock,
        logout,
        error,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
