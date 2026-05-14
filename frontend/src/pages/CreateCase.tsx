import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowLeft, Send, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { ErrorAlert } from '@/components/shared/ErrorAlert';
import { useMutation } from '@/hooks/useApi';
import { incidentApi } from '@/api/endpoints';
import type { IncidentCategory, Priority } from '@/types';

const categories: { value: IncidentCategory; label: string }[] = [
  { value: 'MAINTENANCE', label: 'Maintenance' },
  { value: 'SAFETY', label: 'Safety' },
  { value: 'NOISE', label: 'Noise' },
  { value: 'INTERNET', label: 'Internet' },
  { value: 'BILLING', label: 'Billing' },
  { value: 'DEPOSIT', label: 'Deposit' },
  { value: 'CLEANING', label: 'Cleaning' },
  { value: 'ACCESS', label: 'Access' },
  { value: 'OTHER', label: 'Other' },
];

const priorities: { value: Priority; label: string; description: string }[] = [
  { value: 'CRITICAL', label: 'Critical', description: 'Emergency — immediate response required' },
  { value: 'HIGH', label: 'High', description: 'Urgent — respond within 4 hours' },
  { value: 'MEDIUM', label: 'Medium', description: 'Standard — respond within 24 hours' },
  { value: 'LOW', label: 'Low', description: 'Low priority — respond within 48 hours' },
];

export function CreateCase() {
  const navigate = useNavigate();
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState<string>('');
  const [priority, setPriority] = useState<string>('');

  const createCase = useMutation(
    async (data: { title: string; description: string; category: IncidentCategory; priority?: Priority }) => {
      return incidentApi.create(data);
    },
  );

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || !description.trim() || createCase.isLoading) return;

    try {
      const result = await createCase.execute({
        title: title.trim(),
        description: description.trim(),
        ...(category ? { category: category as IncidentCategory } : {}),
        ...(priority ? { priority: priority as Priority } : {}),
      });
      navigate(`/cases/${result.id}`);
    } catch {
      // Error is already captured by useMutation and shown via createCase.error
    }
  };

  return (
    <div className="max-w-2xl space-y-6">
      <Link to="/cases" className="inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground transition-colors">
        <ArrowLeft className="h-4 w-4" />
        Back to Cases
      </Link>

      <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }}>
        <h2 className="text-2xl font-semibold tracking-tight">Report a New Case</h2>
        <p className="text-sm text-muted-foreground mt-1">Submit a repair request, complaint, or query. Our team will respond based on urgency.</p>
      </motion.div>

      {createCase.error && <ErrorAlert message={createCase.error} />}

      <motion.form
        onSubmit={handleSubmit}
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
        className="rounded-2xl border bg-card p-6 space-y-6"
      >
        <div className="space-y-2">
          <Label htmlFor="title">Title</Label>
          <Input
            id="title"
            value={title}
            onChange={e => setTitle(e.target.value)}
            placeholder="e.g. Heating not working in Flat B204"
            className="h-11 rounded-xl"
            required
            disabled={createCase.isLoading}
          />
          <p className="text-xs text-muted-foreground">Briefly describe the issue in one line</p>
        </div>

        <div className="space-y-2">
          <Label htmlFor="description">Description</Label>
          <Textarea
            id="description"
            value={description}
            onChange={e => setDescription(e.target.value)}
            placeholder="Provide as much detail as possible — location, when it started, severity..."
            className="rounded-xl min-h-[120px] resize-none"
            required
            disabled={createCase.isLoading}
          />
        </div>

        <div className="grid sm:grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label>Category (optional)</Label>
            <Select value={category} onValueChange={(v) => setCategory(v ?? '')} disabled={createCase.isLoading}>
              <SelectTrigger className="h-11 rounded-xl">
                <SelectValue placeholder="Auto-detect" />
              </SelectTrigger>
              <SelectContent className="rounded-xl">
                {categories.map(c => (
                  <SelectItem key={c.value} value={c.value}>{c.label}</SelectItem>
                ))}
              </SelectContent>
            </Select>
            <p className="text-xs text-muted-foreground">Leave blank to auto-classify based on your description</p>
          </div>

          <div className="space-y-2">
            <Label>Urgency (optional)</Label>
            <Select value={priority} onValueChange={(v) => setPriority(v ?? '')} disabled={createCase.isLoading}>
              <SelectTrigger className="h-11 rounded-xl">
                <SelectValue placeholder="Auto-detect" />
              </SelectTrigger>
              <SelectContent className="rounded-xl">
                {priorities.map(p => (
                  <SelectItem key={p.value} value={p.value}>
                    <div>
                      <span>{p.label}</span>
                      <span className="text-muted-foreground ml-2 text-xs">— {p.description}</span>
                    </div>
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <p className="text-xs text-muted-foreground">Leave blank for automatic classification based on keywords</p>
          </div>
        </div>

        <div className="flex items-center gap-3 pt-2">
          <Button type="submit" className="rounded-xl" disabled={createCase.isLoading || !title.trim()}>
            {createCase.isLoading ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Submitting...
              </>
            ) : (
              <>
                <Send className="mr-2 h-4 w-4" />
                Submit Case
              </>
            )}
          </Button>
          <Button type="button" variant="outline" className="rounded-xl" onClick={() => navigate('/cases')} disabled={createCase.isLoading}>
            Cancel
          </Button>
        </div>
      </motion.form>

      {/* Tips */}
      <motion.div
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2 }}
        className="rounded-2xl border bg-muted/30 p-6"
      >
        <h3 className="text-sm font-semibold mb-3">Tips for faster resolution</h3>
        <ul className="space-y-2 text-sm text-muted-foreground">
          <li className="flex gap-2">
            <span className="text-foreground">1.</span>
            Include your flat/room number in the title
          </li>
          <li className="flex gap-2">
            <span className="text-foreground">2.</span>
            Describe when the issue started and how it affects you
          </li>
          <li className="flex gap-2">
            <span className="text-foreground">3.</span>
            Mention if the issue is getting worse or is contained
          </li>
          <li className="flex gap-2">
            <span className="text-foreground">4.</span>
            For emergencies (gas leaks, floods, security), call the emergency line directly
          </li>
        </ul>
      </motion.div>
    </div>
  );
}
