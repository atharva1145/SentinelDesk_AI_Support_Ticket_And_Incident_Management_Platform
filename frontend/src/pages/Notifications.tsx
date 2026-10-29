import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { notificationApi } from '../api/apiService';
import { Notification } from '../api/types';
import { Bell, CheckCheck, ExternalLink, Inbox } from 'lucide-react';

export const Notifications: React.FC = () => {
  const navigate = useNavigate();
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(true);

  const loadNotifications = async () => {
    setLoading(true);
    try {
      const res = await notificationApi.getNotifications();
      setNotifications(res);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadNotifications();
  }, []);

  const handleMarkAll = async () => {
    try {
      await notificationApi.markAllRead();
      loadNotifications();
    } catch (err) {
      console.error(err);
    }
  };

  const handleNotificationClick = async (notif: Notification) => {
    if (!notif.isRead) {
      await notificationApi.markAsRead(notif.id);
    }
    if (notif.link) {
      navigate(notif.link);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">Notifications Center</h1>
          <p className="text-xs text-slate-500">Live operational alerts, SLA breach warnings, and ticket updates</p>
        </div>
        <button
          onClick={handleMarkAll}
          className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold rounded-lg transition-colors"
        >
          <CheckCheck className="w-4 h-4" />
          <span>Mark All as Read</span>
        </button>
      </div>

      <div className="bg-white rounded-xl border border-slate-200 shadow-xs divide-y divide-slate-100 overflow-hidden">
        {loading ? (
          <div className="p-12 text-center">
            <div className="w-6 h-6 border-3 border-sky-600 border-t-transparent rounded-full animate-spin mx-auto" />
          </div>
        ) : notifications.length === 0 ? (
          <div className="p-12 text-center text-slate-400">
            <Inbox className="w-10 h-10 mx-auto text-slate-300 mb-2" />
            <p className="text-sm font-semibold text-slate-700">No notifications</p>
            <p className="text-xs mt-1">You are all caught up with your support activities</p>
          </div>
        ) : (
          notifications.map((n) => (
            <div
              key={n.id}
              onClick={() => handleNotificationClick(n)}
              className={`p-4 flex items-start justify-between gap-4 hover:bg-slate-50 cursor-pointer transition-colors ${
                !n.isRead ? 'bg-sky-50/40' : ''
              }`}
            >
              <div className="flex items-start gap-3">
                <div
                  className={`w-2 h-2 rounded-full mt-2 shrink-0 ${
                    !n.isRead ? 'bg-sky-600 ring-4 ring-sky-100' : 'bg-slate-300'
                  }`}
                />
                <div>
                  <h4 className="text-sm font-semibold text-slate-900">{n.title}</h4>
                  <p className="text-xs text-slate-600 mt-1">{n.message}</p>
                  <span className="text-[10px] text-slate-400 mt-2 block">
                    {new Date(n.createdAt).toLocaleString()}
                  </span>
                </div>
              </div>

              {n.link && (
                <span className="text-slate-400 hover:text-sky-600 p-1">
                  <ExternalLink className="w-4 h-4" />
                </span>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  );
};
