import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Button } from '@/components/ui/button';
import {
  Building2, ArrowRight, ShieldCheck, BarChart3, ClipboardList,
  Zap, Clock, Users,
} from 'lucide-react';
import { useAuth } from '@/contexts/AuthContext';

const features = [
  {
    icon: ClipboardList,
    title: 'Case Lifecycle',
    description: 'Track every repair request and complaint from report to resolution with structured status workflows.',
  },
  {
    icon: ShieldCheck,
    title: 'Role-Based Access',
    description: 'Four distinct roles with enforced permissions. Residents, staff, and managers each see exactly what they should.',
  },
  {
    icon: BarChart3,
    title: 'Operations Dashboard',
    description: 'Real-time statistics on open cases, response times, staff workload, and category distribution.',
  },
  {
    icon: Zap,
    title: 'Smart Classification',
    description: 'Automatic urgency classification using keyword matching. Critical issues are flagged immediately.',
  },
  {
    icon: Clock,
    title: 'Full Audit Trail',
    description: 'Every action is logged — assignments, status changes, comments. Complete accountability.',
  },
  {
    icon: Users,
    title: 'Team Coordination',
    description: 'Assign cases to maintenance staff, track workload balance, and ensure nothing falls through the cracks.',
  },
];

const container = {
  hidden: {},
  show: { transition: { staggerChildren: 0.08 } },
};

const item = {
  hidden: { opacity: 0, y: 20 },
  show: { opacity: 1, y: 0, transition: { duration: 0.5, ease: [0.25, 0.46, 0.45, 0.94] as [number, number, number, number] } },
};

export function Landing() {
  const navigate = useNavigate();
  const { loginWithMock } = useAuth();

  const handleViewDemo = () => {
    loginWithMock('carol@property.io');
    navigate('/dashboard');
  };

  return (
    <div className="min-h-screen bg-background">
      {/* Nav */}
      <nav className="fixed top-0 w-full z-50 border-b bg-background/60 backdrop-blur-xl">
        <div className="mx-auto max-w-6xl flex items-center justify-between h-16 px-6">
          <div className="flex items-center gap-3">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-foreground">
              <Building2 className="h-4 w-4 text-background" />
            </div>
            <span className="text-sm font-semibold tracking-tight">ResolveHub</span>
          </div>
          <div className="flex items-center gap-3">
            <Link to="/login">
              <Button variant="ghost" size="sm">Sign in</Button>
            </Link>
            <Link to="/login">
              <Button size="sm">Get Started</Button>
            </Link>
          </div>
        </div>
      </nav>

      {/* Hero */}
      <section className="relative pt-32 pb-20 px-6">
        <div className="absolute inset-0 -z-10 overflow-hidden">
          <div className="absolute top-1/4 left-1/2 -translate-x-1/2 h-[600px] w-[600px] rounded-full bg-gradient-to-br from-violet-200/30 to-sky-200/30 blur-3xl dark:from-violet-900/20 dark:to-sky-900/20" />
        </div>
        <div className="mx-auto max-w-3xl text-center">
          <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.6 }}>
            <div className="inline-flex items-center gap-2 rounded-full border bg-card px-4 py-1.5 text-xs font-medium text-muted-foreground mb-8">
              <span className="h-1.5 w-1.5 rounded-full bg-emerald-500" />
              Property operations, simplified
            </div>
          </motion.div>

          <motion.h1
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1, duration: 0.6 }}
            className="text-4xl sm:text-5xl lg:text-6xl font-semibold tracking-tight leading-[1.1]"
          >
            Resolve resident issues{' '}
            <span className="text-muted-foreground">before they escalate</span>
          </motion.h1>

          <motion.p
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2, duration: 0.6 }}
            className="mt-6 text-lg text-muted-foreground max-w-xl mx-auto leading-relaxed"
          >
            ResolveHub gives accommodation providers a structured workflow for repair requests and complaints. Every case tracked, every action logged, every team member aligned.
          </motion.p>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.3, duration: 0.6 }}
            className="mt-10 flex items-center justify-center gap-4"
          >
            <Link to="/login">
              <Button size="lg" className="rounded-xl px-8">
                Get Started
                <ArrowRight className="ml-2 h-4 w-4" />
              </Button>
            </Link>
            <Button variant="outline" size="lg" className="rounded-xl px-8" onClick={handleViewDemo}>
              View Demo
            </Button>
          </motion.div>
        </div>
      </section>

      {/* Features */}
      <section className="py-20 px-6">
        <div className="mx-auto max-w-6xl">
          <div className="text-center mb-16">
            <h2 className="text-2xl sm:text-3xl font-semibold tracking-tight">Everything you need to manage resident cases</h2>
            <p className="mt-3 text-muted-foreground max-w-lg mx-auto">From repair requests to complaint resolution, ResolveHub handles the full lifecycle with built-in accountability.</p>
          </div>

          <motion.div
            variants={container}
            initial="hidden"
            whileInView="show"
            viewport={{ once: true, margin: '-100px' }}
            className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6"
          >
            {features.map(f => (
              <motion.div
                key={f.title}
                variants={item}
                className="group rounded-2xl border bg-card p-6 transition-all hover:border-foreground/10 hover:shadow-sm"
              >
                <div className="h-10 w-10 rounded-xl bg-muted/50 flex items-center justify-center mb-4 group-hover:bg-muted transition-colors">
                  <f.icon className="h-5 w-5 text-foreground" />
                </div>
                <h3 className="font-semibold tracking-tight">{f.title}</h3>
                <p className="mt-2 text-sm text-muted-foreground leading-relaxed">{f.description}</p>
              </motion.div>
            ))}
          </motion.div>
        </div>
      </section>

      {/* CTA */}
      <section className="py-20 px-6">
        <div className="mx-auto max-w-2xl text-center">
          <h2 className="text-2xl sm:text-3xl font-semibold tracking-tight">Ready to streamline your operations?</h2>
          <p className="mt-3 text-muted-foreground">Join property managers who have cut resolution times by managing cases in one place.</p>
          <div className="mt-8">
            <Link to="/login">
              <Button size="lg" className="rounded-xl px-8">
                Get Started
                <ArrowRight className="ml-2 h-4 w-4" />
              </Button>
            </Link>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t py-8 px-6">
        <div className="mx-auto max-w-6xl flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Building2 className="h-4 w-4" />
            <span className="text-sm text-muted-foreground">ResolveHub</span>
          </div>
          <p className="text-xs text-muted-foreground">Resident Repair & Complaint Operations Platform</p>
        </div>
      </footer>
    </div>
  );
}
