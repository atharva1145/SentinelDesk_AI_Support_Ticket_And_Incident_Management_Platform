import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { slaApi, ticketApi } from '../api/apiService';
import { SlaPolicy, Ticket } from '../api/types';
import { PriorityBadge } from '../components/PriorityBadge';
import { SlaBadge } from '../components/SlaBadge';
import { Modal } from '../components/Modal';
import { useAuth } from '../context/AuthContext';
import { Clock, ShieldAlert, CheckCircle2, AlertTriangle, Edit3, Settings } from 'lucide-react';

export const SlaMonitoring: React.FC = () => {
  const navigate = useNavigate();
  const { isManager } = useAuth();
  const [policies, setPolicies] = useState<SlaPolicy[]>([]);
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [loading, setLoading] = useState(true);

  const [editingPolicy, setEditingPolicy] = useState<SlaPolicy | null>(null);
  const [firstResponseHours, setFirstResponseHours] = useState<number>(0);
  const [resolutionHours, setResolutionHours] = useState<number>(0);
  const [policyDesc, setPolicyDesc] = useState('');
  const [isBusinessHours, setIsBusinessHours] = useState(true);

  const loadSlaData = async () => {
    setLoading(true);
    try {
      const [polRes, tickRes] = await Promise.all([
        slaApi.getPolicies(),
        ticketApi.getTickets({ size: 50, sortBy: 'createdAt', sortDirection: 'desc' }),
      ]);
      setPolicies(polRes);
      // Filter to tickets that have an SLA tracker and are not closed/resolved
      setTickets(tickRes.content.filter((t) => t.slaTracker));
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSlaData();
    const interval = setInterval(loadSlaData, 20000); // 20s live countdown refresh
    return () => clearInterval(interval);
  }, []);

  const handleSavePolicy = async () => {
    if (!editingPolicy) return;
    try {
      await slaApi.updatePolicy(editingPolicy.id, {
        firstResponseHours,
        resolutionHours,
        description: policyDesc,
        isBusinessHours,
      });
      setEditingPolicy(null);
      loadSlaData();
    } catch (err: any) {
      alert(err.message || 'Failed to update SLA policy');
    }
  };

  const breachedTickets = tickets.filter(
    (t) => t.slaTracker?.resolutionBreached || t.slaTracker?.firstResponseBreached || t.slaTracker?.isOverdue
  );
  const warningTickets = tickets.filter(
    (t) => t.slaTracker?.warningIssued && !t.slaTracker?.resolutionBreached && !t.slaTracker?.isOverdue
  );

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">SLA Performance Engine</h1>
          <p className="text-xs text-slate-500">
            Real-time Service Level Agreement enforcement, policy configurations, and deadline monitoring
          </p>
        </div>
        <div className="flex items-center gap-2">
          <span className="px-3 py-1 bg-rose-50 text-rose-700 text-xs font-semibold rounded-full border border-rose-200">
            {breachedTickets.length} Breaches
          </span>
          <span className="px-3 py-1 bg-amber-50 text-amber-700 text-xs font-semibold rounded-full border border-amber-200">
            {warningTickets.length} Approaching SLA
          </span>
        </div>
      </div>

      {/* SLA Policies Matrix */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="p-4 border-b border-slate-100 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Clock className="w-4 h-4 text-sky-600" />
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-800">
              Configured SLA Priority Targets
            </h3>
          </div>
          <span className="text-[11px] text-slate-400">Enforced continuously by background scheduler</span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 text-slate-500 text-xs font-semibold uppercase tracking-wider border-b border-slate-100">
              <tr>
                <th className="py-3 px-4">Priority Policy</th>
                <th className="py-3 px-4">Schedule Engine</th>
                <th className="py-3 px-4">First Response Target</th>
                <th className="py-3 px-4">Resolution Target</th>
                <th className="py-3 px-4">Policy Description</th>
                {isManager && <th className="py-3 px-4 text-right">Action</th>}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-slate-700">
              {policies.map((p) => (
                <tr key={p.id} className="hover:bg-slate-50/80 transition-colors">
                  <td className="py-3.5 px-4 whitespace-nowrap">
                    <PriorityBadge priority={p.priority} />
                  </td>
                  <td className="py-3.5 px-4 whitespace-nowrap">
                    {p.isBusinessHours ? (
                      <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-blue-50 text-blue-700 border border-blue-200">
                        🕒 Business Hours (9–6 M–F)
                      </span>
                    ) : (
                      <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-purple-50 text-purple-700 border border-purple-200">
                        ⚡ 24x7 Continuous
                      </span>
                    )}
                  </td>
                  <td className="py-3.5 px-4 whitespace-nowrap font-semibold text-slate-800 text-xs">
                    {p.firstResponseHours < 1
                      ? `${Math.round(p.firstResponseHours * 60)} minutes`
                      : `${p.firstResponseHours} hours`}
                  </td>
                  <td className="py-3.5 px-4 whitespace-nowrap font-semibold text-slate-800 text-xs">
                    {p.resolutionHours < 24
                      ? `${p.resolutionHours} hours`
                      : `${p.resolutionHours / 24} business days (${p.resolutionHours}h)`}
                  </td>
                  <td className="py-3.5 px-4 text-xs text-slate-500 max-w-md">{p.description}</td>
                  {isManager && (
                    <td className="py-3.5 px-4 text-right whitespace-nowrap">
                      <button
                        onClick={() => {
                          setEditingPolicy(p);
                          setFirstResponseHours(p.firstResponseHours);
                          setResolutionHours(p.resolutionHours);
                          setPolicyDesc(p.description || '');
                          setIsBusinessHours(p.isBusinessHours ?? true);
                        }}
                        className="inline-flex items-center gap-1 px-2.5 py-1 text-xs font-semibold text-sky-600 hover:text-sky-700 bg-sky-50 hover:bg-sky-100 rounded-md transition-colors"
                      >
                        <Edit3 className="w-3.5 h-3.5" />
                        <span>Configure</span>
                      </button>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Live Active Tickets SLA Countdown Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="p-4 border-b border-slate-100 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <ShieldAlert className="w-4 h-4 text-slate-600" />
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-800">
              Active SLA Countdown & Compliance Trackers
            </h3>
          </div>
          <span className="text-[11px] text-slate-400">Live 20-second heartbeat check</span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 text-slate-500 text-xs font-semibold uppercase tracking-wider border-b border-slate-100">
              <tr>
                <th className="py-3 px-4">Ticket</th>
                <th className="py-3 px-4">Priority</th>
                <th className="py-3 px-4">First Response Status</th>
                <th className="py-3 px-4">Resolution Deadline</th>
                <th className="py-3 px-4">Remaining Window</th>
                <th className="py-3 px-4">Assignee</th>
                <th className="py-3 px-4">Escalation Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-slate-700">
              {tickets.map((t) => {
                const trk = t.slaTracker!;
                return (
                  <tr
                    key={t.id}
                    onClick={() => navigate(`/tickets/${t.id}`)}
                    className="hover:bg-slate-50/80 cursor-pointer transition-colors"
                  >
                    <td className="py-3.5 px-4 max-w-xs">
                      <span className="font-mono text-xs font-bold text-sky-600 block">{t.ticketNumber}</span>
                      <span className="font-medium text-slate-800 line-clamp-1">{t.title}</span>
                    </td>
                    <td className="py-3.5 px-4 whitespace-nowrap">
                      <PriorityBadge priority={t.priority} />
                    </td>
                    <td className="py-3.5 px-4 whitespace-nowrap text-xs">
                      {trk.firstResponseMet ? (
                        <span className="text-emerald-700 font-semibold flex items-center gap-1">
                          <CheckCircle2 className="w-3.5 h-3.5" /> Met (
                          {new Date(t.firstResponseAt!).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })})
                        </span>
                      ) : trk.firstResponseBreached ? (
                        <span className="text-rose-700 font-bold flex items-center gap-1">
                          <AlertTriangle className="w-3.5 h-3.5" /> Breached
                        </span>
                      ) : (
                        <span className="text-slate-500">
                          Due: {new Date(trk.firstResponseDeadline).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                        </span>
                      )}
                    </td>
                    <td className="py-3.5 px-4 whitespace-nowrap text-xs text-slate-600">
                      {new Date(trk.resolutionDeadline).toLocaleString()}
                    </td>
                    <td className="py-3.5 px-4 whitespace-nowrap">
                      <SlaBadge tracker={trk} />
                    </td>
                    <td className="py-3.5 px-4 whitespace-nowrap text-xs text-slate-600">
                      {t.assignedAgent?.fullName || <span className="text-amber-600 italic">Unassigned</span>}
                    </td>
                    <td className="py-3.5 px-4 whitespace-nowrap">
                      {trk.isEscalated ? (
                        <span className="px-2 py-0.5 rounded-full text-xs font-bold bg-rose-100 text-rose-800">
                          Escalated
                        </span>
                      ) : (
                        <span className="text-xs text-slate-400">Normal</span>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      {/* Edit Policy Modal */}
      <Modal
        isOpen={!!editingPolicy}
        onClose={() => setEditingPolicy(null)}
        title={`Configure SLA Target: ${editingPolicy?.name || ''}`}
      >
        <div className="space-y-4 text-xs">
          <div>
            <label className="block font-semibold uppercase text-slate-600 mb-1">
              First Response Target (Hours)
            </label>
            <input
              type="number"
              step="0.05"
              value={firstResponseHours}
              onChange={(e) => setFirstResponseHours(Number(e.target.value))}
              className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg text-xs"
            />
            <span className="text-[10px] text-slate-400">0.25 = 15 minutes, 1.0 = 1 hour, 4.0 = 4 hours</span>
          </div>

          <div>
            <label className="block font-semibold uppercase text-slate-600 mb-1">
              Resolution Target (Hours)
            </label>
            <input
              type="number"
              step="0.5"
              value={resolutionHours}
              onChange={(e) => setResolutionHours(Number(e.target.value))}
              className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg text-xs"
            />
            <span className="text-[10px] text-slate-400">4.0 = 4 hours, 8.0 = 8 hours, 48.0 = 2 days</span>
          </div>

          <div>
            <label className="block font-semibold uppercase text-slate-600 mb-1">Description / Commitment</label>
            <textarea
              rows={3}
              value={policyDesc}
              onChange={(e) => setPolicyDesc(e.target.value)}
              className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg text-xs"
            />
          </div>

          <div className="flex items-center gap-2 pt-1">
            <input
              type="checkbox"
              id="isBusinessHours"
              checked={isBusinessHours}
              onChange={(e) => setIsBusinessHours(e.target.checked)}
              className="rounded border-slate-300 text-sky-600 focus:ring-sky-500"
            />
            <label htmlFor="isBusinessHours" className="text-xs text-slate-700 font-semibold cursor-pointer">
              Enforce Business Hours (Mon–Fri 09:00–18:00, exclude weekends & public holidays)
            </label>
          </div>

          <div className="flex justify-end gap-2 pt-2">
            <button
              onClick={() => setEditingPolicy(null)}
              className="px-3 py-1.5 text-slate-600 hover:text-slate-800"
            >
              Cancel
            </button>
            <button
              onClick={handleSavePolicy}
              className="px-4 py-1.5 bg-sky-600 hover:bg-sky-700 text-white font-semibold rounded-lg"
            >
              Save Policy Targets
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
