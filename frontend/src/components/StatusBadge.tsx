import React from 'react';
import { TicketStatus } from '../api/types';

interface StatusBadgeProps {
  status: TicketStatus;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status }) => {
  const config: Record<TicketStatus, { label: string; className: string }> = {
    NEW: {
      label: 'New',
      className: 'bg-indigo-50 text-indigo-700 border-indigo-200',
    },
    TRIAGED: {
      label: 'Triaged',
      className: 'bg-purple-50 text-purple-700 border-purple-200',
    },
    OPEN: {
      label: 'Open',
      className: 'bg-sky-50 text-sky-700 border-sky-200',
    },
    IN_PROGRESS: {
      label: 'In Progress',
      className: 'bg-amber-50 text-amber-700 border-amber-200',
    },
    PENDING_REQUESTER: {
      label: 'Pending Requester',
      className: 'bg-teal-50 text-teal-700 border-teal-200',
    },
    RESOLVED: {
      label: 'Resolved',
      className: 'bg-emerald-50 text-emerald-700 border-emerald-200',
    },
    CLOSED: {
      label: 'Closed',
      className: 'bg-slate-100 text-slate-700 border-slate-200',
    },
    CANCELLED: {
      label: 'Cancelled',
      className: 'bg-rose-50 text-rose-700 border-rose-200',
    },
  };

  const item = config[status] || {
    label: status,
    className: 'bg-slate-100 text-slate-700 border-slate-200',
  };

  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${item.className}`}
    >
      {item.label}
    </span>
  );
};
