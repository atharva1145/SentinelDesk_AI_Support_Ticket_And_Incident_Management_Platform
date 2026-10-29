import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AlertTriangle, Bell, CheckCircle, Info, Sparkles, X } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

interface ToastItem {
  id: string;
  type: 'NOTIFICATION' | 'TICKET_CREATED' | 'SLA_ALERT' | 'INFO';
  title: string;
  message: string;
  link?: string;
  timestamp: Date;
}

export const LiveAlertToast: React.FC = () => {
  const { user, token } = useAuth();
  const navigate = useNavigate();
  const [toasts, setToasts] = useState<ToastItem[]>([]);

  useEffect(() => {
    if (!user || !token) return;

    let eventSource: EventSource | null = null;
    try {
      // EventSource with standard authorization header or connection
      eventSource = new EventSource(`/api/v1/notifications/stream`);

      eventSource.addEventListener('NOTIFICATION', (e: MessageEvent) => {
        try {
          const data = JSON.parse(e.data);
          addToast({
            id: Math.random().toString(),
            type: 'NOTIFICATION',
            title: data.title || 'New Notification',
            message: data.message || '',
            link: data.link,
            timestamp: new Date(),
          });
        } catch (err) {}
      });

      eventSource.addEventListener('TICKET_CREATED', (e: MessageEvent) => {
        try {
          const data = JSON.parse(e.data);
          addToast({
            id: Math.random().toString(),
            type: 'TICKET_CREATED',
            title: `New Ticket Created: ${data.ticketNumber}`,
            message: `${data.title} (${data.priority} - ${data.category})`,
            timestamp: new Date(),
          });
        } catch (err) {}
      });

      eventSource.addEventListener('SLA_ALERT', (e: MessageEvent) => {
        try {
          const data = JSON.parse(e.data);
          addToast({
            id: Math.random().toString(),
            type: 'SLA_ALERT',
            title: `SLA Escalation Alert [${data.escalationLevel}]`,
            message: `Ticket #${data.ticketNumber} (${data.priority}): ${data.reason}`,
            timestamp: new Date(),
          });
        } catch (err) {}
      });
    } catch (err) {
      console.warn('SSE stream connection notice:', err);
    }

    return () => {
      if (eventSource) {
        eventSource.close();
      }
    };
  }, [user, token]);

  const addToast = (toast: ToastItem) => {
    setToasts((prev) => [toast, ...prev].slice(0, 5));
    setTimeout(() => {
      removeToast(toast.id);
    }, 7000);
  };

  const removeToast = (id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  };

  if (toasts.length === 0) return null;

  return (
    <div className="fixed top-20 right-6 z-50 flex flex-col gap-3 max-w-sm w-full pointer-events-none">
      {toasts.map((toast) => (
        <div
          key={toast.id}
          onClick={() => {
            if (toast.link) navigate(toast.link);
            removeToast(toast.id);
          }}
          className={`pointer-events-auto p-4 rounded-xl shadow-2xl border backdrop-blur-md transition-all transform animate-in slide-in-from-top duration-300 cursor-pointer ${
            toast.type === 'SLA_ALERT'
              ? 'bg-rose-950/90 border-rose-500/50 text-rose-100 shadow-rose-950/50'
              : toast.type === 'TICKET_CREATED'
              ? 'bg-indigo-950/90 border-indigo-500/50 text-indigo-100 shadow-indigo-950/50'
              : 'bg-slate-900/90 border-slate-700/80 text-slate-100 shadow-black/60'
          }`}
        >
          <div className="flex items-start gap-3">
            <div className="p-1.5 rounded-lg shrink-0 mt-0.5">
              {toast.type === 'SLA_ALERT' && (
                <div className="bg-rose-500/20 text-rose-400 p-1.5 rounded-lg">
                  <AlertTriangle className="w-5 h-5 animate-pulse" />
                </div>
              )}
              {toast.type === 'TICKET_CREATED' && (
                <div className="bg-indigo-500/20 text-indigo-400 p-1.5 rounded-lg">
                  <Sparkles className="w-5 h-5" />
                </div>
              )}
              {toast.type === 'NOTIFICATION' && (
                <div className="bg-amber-500/20 text-amber-400 p-1.5 rounded-lg">
                  <Bell className="w-5 h-5" />
                </div>
              )}
              {toast.type === 'INFO' && (
                <div className="bg-blue-500/20 text-blue-400 p-1.5 rounded-lg">
                  <Info className="w-5 h-5" />
                </div>
              )}
            </div>

            <div className="flex-1 min-w-0">
              <div className="flex items-center justify-between gap-2">
                <h4 className="text-sm font-semibold truncate">{toast.title}</h4>
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    removeToast(toast.id);
                  }}
                  className="text-slate-400 hover:text-white transition-colors"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
              <p className="text-xs text-slate-300 mt-1 line-clamp-2">{toast.message}</p>
              <span className="text-[10px] text-slate-400 mt-2 block">
                {toast.timestamp.toLocaleTimeString()} · Click to view
              </span>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
};
