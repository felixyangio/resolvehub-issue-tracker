import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Button } from '@/components/ui/button';
import { ArrowLeft, FileQuestion } from 'lucide-react';

export function NotFound() {
  return (
    <div className="min-h-screen flex items-center justify-center p-6">
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        className="text-center max-w-md"
      >
        <div className="mx-auto h-16 w-16 rounded-2xl bg-muted flex items-center justify-center mb-6">
          <FileQuestion className="h-8 w-8 text-muted-foreground" />
        </div>
        <h1 className="text-4xl font-semibold tracking-tight">404</h1>
        <p className="text-muted-foreground mt-2">This page doesn&apos;t exist or you don&apos;t have permission to view it.</p>
        <div className="flex items-center justify-center gap-3 mt-6">
          <Link to="/dashboard">
            <Button className="rounded-xl">
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back to Dashboard
            </Button>
          </Link>
          <Link to="/">
            <Button variant="outline" className="rounded-xl">Home</Button>
          </Link>
        </div>
      </motion.div>
    </div>
  );
}
