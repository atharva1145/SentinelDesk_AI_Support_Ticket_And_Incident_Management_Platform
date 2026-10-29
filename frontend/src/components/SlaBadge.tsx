import React from 'react';
import { SlaTracker } from '../api/types';
import { Clock, AlertOctagon, CheckCircle2 } from 'lucide-react';

interface SlaBadgeProps {
  tracker?: SlaTracker;
}

export const SlaBadge: React.FC<SlaBadgeProps> = ({ tracker }) => {
  if (!tracker) {
    return (
      <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs text-slate-500 bg-slate-100">
        <Clock className="w-3 h-3" /> Unset
      </span>
    );
  }

  if (tracker.resolutionMet) {
    return (
      <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-emerald-50 text-emerald-700 border border-emerald-200">
        <CheckCircle2 className="w-3 h-3" /> Met SLA
      </span>
    );
  }

  if (tracker.resolutionBreached || tracker.isOverdue) {
    return (
      <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-rose-50 text-rose-700 border border-rose-200 animate-pulse">
        <AlertOctagon className="w-3 h-3 text-rose-600" /> SLA Breached
      </span>
    );
  }

  const mins = tracker.remainingMinutes;
  const hours = Math.floor(mins / 60);
  const remainingMins = mins % 60;
  const timeText = hours > 0 ? `${hours}h ${remainingMins}m` : `${mins}m`;

  if (tracker.warningIssued || mins < 60) {
    return (
      <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-amber-50 text-amber-800 border border-amber-300">
        <Clock className="w-3 h-3 text-amber-600" /> {timeText} remaining
      </span>
    );
  }

  return (
    <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-sky-50 text-sky-700 border border-sky-200">
      <Clock className="w-3 h-3" /> {timeText} left
    </span>
  );
};
