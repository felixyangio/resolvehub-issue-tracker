import { createBrowserRouter } from 'react-router-dom';
import { AppShell } from '@/layouts/AppShell';
import { ProtectedRoute } from '@/components/shared/ProtectedRoute';
import { Landing } from '@/pages/Landing';
import { Login } from '@/pages/Login';
import { Register } from '@/pages/Register';
import { Dashboard } from '@/pages/Dashboard';
import { CaseList } from '@/pages/CaseList';
import { CaseDetail } from '@/pages/CaseDetail';
import { CreateCase } from '@/pages/CreateCase';
import { Settings } from '@/pages/Settings';
import { NotFound } from '@/pages/NotFound';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <Landing />,
  },
  {
    path: '/login',
    element: <Login />,
  },
  {
    path: '/register',
    element: <Register />,
  },
  {
    element: (
      <ProtectedRoute>
        <AppShell />
      </ProtectedRoute>
    ),
    children: [
      { path: '/dashboard', element: <Dashboard /> },
      { path: '/cases', element: <CaseList /> },
      { path: '/cases/new', element: <CreateCase /> },
      { path: '/cases/:id', element: <CaseDetail /> },
      { path: '/settings', element: <Settings /> },
    ],
  },
  {
    path: '*',
    element: <NotFound />,
  },
]);
