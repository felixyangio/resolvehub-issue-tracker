# ResolveHub Frontend

Premium enterprise UI for ResolveHub — Resident Repair & Complaint Operations Platform.

Built with React 19, TypeScript, Vite, Tailwind CSS v4, shadcn/ui, Recharts, Framer Motion, and Lucide icons.

## Quick Start

```bash
cd frontend
npm install
cp .env.example .env    # configure API base URL
npm run dev
```

Open [http://localhost:5173](http://localhost:5173)

## Backend Connection

The frontend connects to the Spring Boot backend API. When the backend is unavailable, pages automatically fall back to mock data so the UI always renders.

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `VITE_API_BASE_URL` | `http://localhost:8080/api` | Backend API base URL |

### Running with Backend

1. Start PostgreSQL: `docker compose up -d`
2. Start Spring Boot: `./mvnw spring-boot:run`
3. Start frontend: `cd frontend && npm run dev`
4. Open [http://localhost:5173](http://localhost:5173)
5. Log in with a demo account (password: `password`)

### Running Frontend Only (Demo Mode)

```bash
cd frontend && npm run dev
```

The frontend will show a yellow "demo data" banner and use mock data for all pages. No backend required.

### API Endpoints Used

| Frontend Page | Backend Endpoints |
|--------------|-------------------|
| Login | `POST /api/auth/login`, `GET /api/users/me` |
| Dashboard | `GET /api/dashboard/summary`, `GET /api/dashboard/incidents-by-status`, `GET /api/dashboard/incidents-by-severity` |
| Case List | `GET /api/incidents?status=&page=&size=` |
| Case Detail | `GET /api/incidents/:id`, `GET /api/incidents/:id/comments`, `GET /api/incidents/:id/audit-logs` |
| Create Case | `POST /api/incidents` |
| Add Comment | `POST /api/incidents/:id/comments` |

### Authentication Flow

1. User submits email + password on the Login page
2. Frontend calls `POST /api/auth/login` and receives a JWT token
3. Token is stored in `localStorage` and attached as `Authorization: Bearer <token>` on all subsequent requests
4. `GET /api/users/me` fetches the user profile
5. On 401 response, the token is cleared and the user is redirected to `/login`
6. Protected routes redirect unauthenticated users to `/login`

## Pages

| Route | Page | Description |
|-------|------|-------------|
| `/` | Landing | Product marketing page |
| `/login` | Login | Authentication with demo accounts |
| `/dashboard` | Dashboard | Operations overview with charts |
| `/cases` | Case List | Filterable, searchable case table |
| `/cases/new` | Create Case | New repair request / complaint form |
| `/cases/:id` | Case Detail | Full case view with comments + timeline |
| `/settings` | Settings | Profile and preferences |
| `*` | 404 | Not found page |

## Architecture

```
src/
  api/
    client.ts          # HTTP client with auth header injection
    endpoints.ts       # Typed API endpoint functions
    mappers.ts         # Backend DTO → frontend type mappers
  components/
    shared/            # Reusable domain components
    ui/                # shadcn/ui primitives
  contexts/
    AuthContext.tsx     # Auth state, login/logout, user info
  data/
    mock.ts            # Mock data fallback for demo mode
  hooks/
    useApi.ts          # Data fetching hook with mock fallback
  layouts/             # AppShell, Sidebar, TopNav
  pages/               # Route-level pages
  routes/              # React Router config with protected routes
  types/               # TypeScript type definitions
  lib/                 # Utility functions
```

## Tech Stack

- **React 19** + TypeScript
- **Vite 8** (build tool)
- **Tailwind CSS v4** (utility-first styling)
- **shadcn/ui** (component primitives)
- **Recharts** (dashboard charts)
- **Framer Motion** (animations)
- **Lucide React** (icons)
- **React Router v7** (client-side routing)

## Design

Apple-inspired premium minimalism with Linear/Stripe dashboard sensibility:

- Clean whitespace and typography hierarchy
- Rounded-2xl cards with subtle borders
- Soft gradient backgrounds
- Glass-like backdrop blur on nav
- Staggered entrance animations
- Professional status/urgency/category badges
- Responsive sidebar + mobile drawer
- Dark mode support via shadcn theme tokens

## Mock Data Fallback

The frontend includes realistic mock data matching the backend domain:
- 12 resident cases across all 9 categories
- Dashboard summary with weekly trends
- Comment threads and audit timeline for INC-001
- Category/status/priority distributions

When the backend is unreachable (network error or 5xx), every page gracefully falls back to mock data with a visible banner. This ensures the UI is always demonstrable — perfect for portfolio reviews and interviews.
