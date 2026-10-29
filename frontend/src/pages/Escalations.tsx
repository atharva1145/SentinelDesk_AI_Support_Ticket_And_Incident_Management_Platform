import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { escalationApi } from '../api/apiService';
import { EscalationEvent } from '../api/types';
import { PriorityBadge } from '../components/PriorityBadge';
import { AlertTriangle, ShieldAlert, CheckCircle, Clock, Check } from 'lucide-react';

export const Escalations: React.FC = () => {
  const navigate = useNavigate();
  const [escalations, setEscalations] = useState<EscalationEvent[]>([]);
  const [loading, setLoading] = useState(true);

  const loadEscalations = async () => {
    setLoading(true);
    try {
      const res = await escalationApi.getEscalations();
      setEscalations(res);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadEscalations();
  }, []);

  const handleUpdateStatus = async (id: number, status: 'ACKNOWLEDGED' | 'RESOLVED') => {
    try {
      await escalationApi.updateStatus(id, status);
      loadEscalations();
    } catch (err: any) {
      alert(err.message || 'Failed to update escalation status');
    }
  };

  const getLevelBadge = (level: string) => {
    switch (level) {
      case 'RESOLUTION_BREACH':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold bg-rose-100 text-rose-800 border border-rose-200">
            <ShieldAlert className="w-3 h-3 text-rose-600" /> Resolution Breach
          </span>
        );
      case 'FIRST_RESPONSE_BREACH':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-100 text-amber-800 border border-amber-200">
            <Clock className="w-3 h-3 text-amber-600" /> First Response Breach
          </span>
        );
      case 'WARNING_80':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-sky-100 text-sky-800 border border-sky-200">
            <AlertTriangle className="w-3 h-3 text-sky-600" /> 80% SLA Warning
          </span>
        );
      default:
        return <span className="text-xs text-slate-600">{level}</span>;
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">Automated SLA Escalations</h1>
          <p className="text-xs text-slate-500">
            Triggered automatically when SLA deadlines are reached without duplicate escalation events
          </p>
        </div>
        <span className="text-xs text-slate-500 bg-white px-3 py-1.5 rounded-lg border border-slate-200 shadow-xs">
          Total Recorded: <span className="font-bold text-slate-800">{escalations.length}</span>
        </span>
      </div>

      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
        {loading ? (
          <div className="p-12 text-center">
            <div className="w-6 h-6 border-3 border-sky-600 border-t-transparent rounded-full animate-spin mx-auto" />
          </div>
        ) : escalations.length === 0 ? (
          <div className="p-12 text-center text-slate-400">
            <CheckCircle className="w-10 h-10 mx-auto text-emerald-500 mb-2" />
            <p className="text-sm font-semibold text-slate-700">No active SLA escalations</p>
            <p className="text-xs mt-1">All support tickets are within target SLA thresholds</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-slate-500 text-xs font-semibold uppercase tracking-wider border-b border-slate-100">
                <tr>
                  <th className="py-3 px-4">Ticket</th>
                  <th className="py-3 px-4">Escalation Level</th>
                  <th className="py-3 px-4">Escalation Reason</th>
                  <th className="py-3 px-4">Escalated To</th>
                  <th className="py-3 px-4">Triggered At</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-slate-700">
                {escalations.map((e) => (
                  <tr key={e.id} className="hover:bg-slate-50/80 transition-colors">
                    <td
                      onClick={() => navigate(`/tickets/${e.ticketId}`)}
                      className="py-3.5 px-4 cursor-pointer max-w-xs"
                    >
                      <span className="font-mono text-xs font-bold text-sky-600 block">{e.ticketNumber}</span>
                      <span className="font-medium text-slate-800 line-clamp-1">{e.ticketTitle}</span>
                    </td>
                    <td className="py-3.5 px-4 whitespace-nowrap">
                      {getLevelBadge(e.escalationLevel)}
                    </td>
                    <td className="py-3.5 px-4 text-xs text-slate-600 max-w-xs">{e.reason}</td>
                    <td className="py-3.5 px-4 whitespace-nowrap text-xs text-slate-600">
                      {e.escalatedTo?.fullName || 'Manager On-Duty'}
                    </td>
                    <td className="py-3.5 px-4 whitespace-nowrap text-xs text-slate-400">
                      {new Date(e.createdAt).toLocaleString()}
                    </td>
                    <td className="py-3.5 px-4 whitespace-nowrap">
                      {e.status === 'RESOLVED' ? (
                        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-emerald-50 text-emerald-700 border border-emerald-200">
                          <Check className="w-3 h-3" /> Resolved
                        </span>
                      ) : e.status === 'ACKNOWLEDGED' ? (
                        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-50 text-blue-700 border border-blue-200">
                          Acknowledged
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold bg-rose-50 text-rose-700 border border-rose-200 animate-pulse">
                          Triggered
                        </span>
                      )}
                    </td>
                    <td className="py-3.5 px-4 text-right whitespace-nowrap">
                      <div className="flex items-center justify-end gap-1.5">
                        {e.status === 'TRIGGERED' && (
                          <button
                            onClick={() => handleUpdateStatus(e.id, 'ACKNOWLEDGED')}
                            className="px-2.5 py-1 text-xs font-semibold text-blue-700 bg-blue-50 hover:bg-blue-100 rounded-md border border-blue-200"
                          >
                            Acknowledge
                          </button>
                        )}
                        {e.status !== 'RESOLVED' && (
                          <button
                            onClick={() => handleUpdateStatus(e.id, 'RESOLVED')}
                            className="px-2.5 py-1 text-xs font-semibold text-emerald-700 bg-emerald-50 hover:bg-emerald-100 rounded-md border border-emerald-200"
                          >
                            Resolve
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
