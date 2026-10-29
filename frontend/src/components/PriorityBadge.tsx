import React from 'react';
import { TicketPriority } from '../api/types';
import { AlertTriangle, AlertCircle, Info, ShieldAlert } from 'lucide-react';

interface PriorityBadgeProps {
  priority: TicketPriority;
  showIcon?: boolean;
}

export const PriorityBadge: React.FC<PriorityBadgeProps> = ({ priority, showIcon = true }) => {
  const config = {
    P1: {
      label: 'P1 · Critical',
      bg: 'bg-rose-50 text-rose-700 border-rose-200 ring-rose-500/20',
      icon: ShieldAlert,
    },
    P2: {
      label: 'P2 · High',
      bg: 'bg-amber-50 text-amber-700 border-amber-200 ring-amber-500/20',
      icon: AlertTriangle,
    },
    P3: {
      label: 'P3 · Medium',
      bg: 'bg-blue-50 text-blue-700 border-blue-200 ring-blue-500/20',
      icon: AlertCircle,
    },
    P4: {
      label: 'P4 · Low',
      bg: 'bg-slate-100 text-slate-700 border-slate-200 ring-slate-500/20',
      icon: Info,
    },
  }[priority] || {
    label: priority,
    bg: 'bg-slate-100 text-slate-700 border-slate-200',
    icon: Info,
  };

  const IconComponent = config.icon;

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-semibold border ${config.bg}`}
    >
      {showIcon && <IconComponent className="w-3.5 h-3.5 shrink-0" />}
      {config.label}
    </span>
  );
};
