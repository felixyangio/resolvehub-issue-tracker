import type { Comment } from '@/types';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { motion } from 'framer-motion';
import { getInitials, formatCommentTime } from '@/lib/utils';
import { ROLE_LABELS_SHORT } from '@/lib/constants';

const roleBg: Record<string, string> = {
  USER: 'bg-blue-100 text-blue-700 dark:bg-blue-950 dark:text-blue-300',
  AGENT: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-950 dark:text-emerald-300',
  MANAGER: 'bg-violet-100 text-violet-700 dark:bg-violet-950 dark:text-violet-300',
  ADMIN: 'bg-amber-100 text-amber-700 dark:bg-amber-950 dark:text-amber-300',
};

export function CommentThread({ comments }: { comments: Comment[] }) {
  return (
    <div className="space-y-4">
      {comments.map((comment, i) => (
        <motion.div
          key={comment.id}
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: i * 0.05, duration: 0.3 }}
          className="flex gap-3"
        >
          <Avatar className="h-8 w-8 shrink-0">
            <AvatarFallback className="text-xs bg-muted">{getInitials(comment.author.name)}</AvatarFallback>
          </Avatar>
          <div className="flex-1 rounded-xl border bg-card p-4">
            <div className="flex items-center gap-2 mb-2">
              <span className="text-sm font-semibold">{comment.author.name}</span>
              <Badge variant="outline" className={`text-[10px] px-1.5 py-0 rounded-full font-medium ${roleBg[comment.authorRole]}`}>
                {ROLE_LABELS_SHORT[comment.authorRole as keyof typeof ROLE_LABELS_SHORT] ?? comment.authorRole}
              </Badge>
              <span className="text-xs text-muted-foreground ml-auto">{formatCommentTime(comment.createdAt)}</span>
            </div>
            <p className="text-sm text-muted-foreground leading-relaxed">{comment.content}</p>
          </div>
        </motion.div>
      ))}
    </div>
  );
}
