import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ticketApi, teamApi, authApi } from '../api/apiService';
import { Ticket, Team, User } from '../api/types';
import { PriorityBadge } from '../components/PriorityBadge';
import { StatusBadge } from '../components/StatusBadge';
import { SlaBadge } from '../components/SlaBadge';
import { Modal } from '../components/Modal';
import { Inbox, UserPlus, ArrowRight, Clock, AlertTriangle, Zap, CheckCircle2 } from 'lucide-react';

export const AssignmentQueue: React.FC = () => {
  const navigate = useNavigate();
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [agents, setAgents] = useState<User[]>([]);
  const [teams, setTeams] = useState<Team[]>([]);
  const [loading, setLoading] = useState(true);

  const [selectedTicket, setSelectedTicket] = useState<Ticket | null>(null);
  const [agentId, setAgentId] = useState('');
  const [teamId, setTeamId] = useState('');
  const [reason, setReason] = useState('');

  const [autoBalancing, setAutoBalancing] = useState(false);
  const [balanceMessage, setBalanceMessage] = useState<string | null>(null);

  const loadQueue = async () => {
    setLoading(true);
    try {
      const [tRes, agRes, tmRes] = await Promise.all([
        ticketApi.getTickets({ unassigned: true, size: 50, sortBy: 'priority', sortDirection: 'asc' }),
        authApi.getStaffAndAgents(),
        teamApi.getTeams(),
      ]);
      setTickets(tRes.content);
      setAgents(agRes);
      setTeams(tmRes);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadQueue();
  }, []);

  const handleAssign = async () => {
    if (!selectedTicket) return;
    try {
      await ticketApi.assignTicket(selectedTicket.id, {
        agentId: agentId ? Number(agentId) : null,
        teamId: teamId ? Number(teamId) : undefined,
        reason: reason || 'Assigned from unassigned triage queue',
      });
      setSelectedTicket(null);
      setAgentId('');
      setTeamId('');
      setReason('');
      loadQueue();
    } catch (err: any) {
      alert(err.message || 'Assignment failed');
    }
  };

  const handleAutoBalanceQueue = async () => {
    setAutoBalancing(true);
    setBalanceMessage(null);
    try {
      const res = await ticketApi.autoBalanceQueue('LEAST_BUSY');
      setBalanceMessage(`Successfully load-balanced ${res.totalAssigned} unassigned ticket(s) across available support staff!`);
      loadQueue();
    } catch (err: any) {
      alert(err.message || 'Auto-balance failed');
    } finally {
      setAutoBalancing(false);
    }
  };

  const handleQuickAutoAssign = async (tId: number) => {
    try {
      const res = await ticketApi.autoAssignTicket(tId, 'LEAST_BUSY');
      setBalanceMessage(`Ticket #${res.ticketNumber} auto-assigned to ${res.assignedAgentName}!`);
      loadQueue();
    } catch (err: any) {
      alert(err.message || 'Quick assign failed');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">Unassigned Ticket Queue</h1>
          <p className="text-xs text-slate-500">
            Open tickets awaiting support agent pickup or team assignment
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            type="button"
            disabled={autoBalancing || tickets.length === 0}
            onClick={handleAutoBalanceQueue}
            className="px-3.5 py-1.5 bg-indigo-600 hover:bg-indigo-700 disabled:bg-slate-300 text-white text-xs font-semibold rounded-lg shadow-xs transition-colors flex items-center gap-1.5"
          >
            <Zap className="w-3.5 h-3.5" />
            <span>{autoBalancing ? 'Balancing Queue...' : 'Auto-Balance All (Least-Busy)'}</span>
          </button>
          <span className="px-3 py-1 bg-amber-50 text-amber-700 text-xs font-semibold rounded-full border border-amber-200 flex items-center gap-1.5">
            <AlertTriangle className="w-3.5 h-3.5 text-amber-600" />
            {tickets.length} unassigned
          </span>
        </div>
      </div>

      {balanceMessage && (
        <div className="p-3 bg-indigo-50 border border-indigo-200 rounded-lg text-xs text-indigo-900 flex items-center justify-between">
          <span className="flex items-center gap-2 font-medium">
            <CheckCircle2 className="w-4 h-4 text-indigo-600 shrink-0" /> {balanceMessage}
          </span>
          <button
            onClick={() => setBalanceMessage(null)}
            className="text-indigo-400 hover:text-indigo-600 font-bold text-xs ml-2"
          >
            ✕
          </button>
        </div>
      )}

      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
        {loading ? (
          <div className="p-12 text-center">
            <div className="w-6 h-6 border-3 border-sky-600 border-t-transparent rounded-full animate-spin mx-auto" />
          </div>
        ) : tickets.length === 0 ? (
          <div className="p-12 text-center text-slate-400">
            <Inbox className="w-10 h-10 mx-auto text-slate-300 mb-2" />
            <p className="text-sm font-semibold text-slate-700">Queue is completely clear!</p>
            <p className="text-xs mt-1">All active support tickets have been assigned to technicians</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-slate-500 text-xs font-semibold uppercase tracking-wider border-b border-slate-100">
                <tr>
                  <th className="py-3 px-4">Ticket</th>
                  <th className="py-3 px-4">Priority</th>
                  <th className="py-3 px-4">Category</th>
                  <th className="py-3 px-4">Requester</th>
                  <th className="py-3 px-4">Team</th>
                  <th className="py-3 px-4">SLA Deadline</th>
                  <th className="py-3 px-4 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-slate-700">
                {tickets.map((t) => (
                  <tr key={t.id} className="hover:bg-slate-50/80 transition-colors">
                    <td
                      onClick={() => navigate(`/tickets/${t.id}`)}
                      className="py-3.5 px-4 cursor-pointer max-w-xs"
                    >
                      <span className="font-mono text-xs font-bold text-sky-600 block">{t.ticketNumber}</span>
                      <span className="font-medium text-slate-800 line-clamp-1">{t.title}</span>
                    </td>
                    <td className="py-3.5 px-4 whitespace-nowrap">
                      <PriorityBadge priority={t.priority} />
                    </td>
                    <td className="py-3.5 px-4 whitespace-nowrap text-xs text-slate-600 font-medium capitalize">
                      {t.category.toLowerCase()}
                    </td>
                    <td className="py-3.5 px-4 whitespace-nowrap text-xs text-slate-600">
                      {t.requester.fullName}
                    </td>
                    <td className="py-3.5 px-4 whitespace-nowrap text-xs text-slate-500">
                      {t.assignedTeam?.name || <span className="text-slate-400 italic">No team</span>}
                    </td>
                    <td className="py-3.5 px-4 whitespace-nowrap">
                      <SlaBadge tracker={t.slaTracker} />
                    </td>
                    <td className="py-3.5 px-4 text-right whitespace-nowrap">
                      <div className="inline-flex items-center gap-1.5 justify-end">
                        <button
                          type="button"
                          onClick={() => handleQuickAutoAssign(t.id)}
                          title="Auto-assign to least-busy agent"
                          className="inline-flex items-center gap-1 px-2.5 py-1.5 bg-indigo-50 hover:bg-indigo-100 text-indigo-700 text-xs font-semibold rounded-lg border border-indigo-200 transition-colors shadow-2xs"
                        >
                          <Zap className="w-3 h-3 text-indigo-600" />
                          <span>Auto-Assign</span>
                        </button>
                        <button
                          onClick={() => {
                            setSelectedTicket(t);
                            setTeamId(t.assignedTeam ? String(t.assignedTeam.id) : '');
                          }}
                          className="inline-flex items-center gap-1 px-2.5 py-1.5 bg-sky-50 hover:bg-sky-100 text-sky-700 text-xs font-semibold rounded-lg border border-sky-200 transition-colors"
                        >
                          <UserPlus className="w-3.5 h-3.5" />
                          <span>Manual</span>
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Quick Assign Modal */}
      <Modal
        isOpen={!!selectedTicket}
        onClose={() => setSelectedTicket(null)}
        title={`Assign Ticket ${selectedTicket?.ticketNumber || ''}`}
      >
        <div className="space-y-4 text-xs">
          <p className="text-slate-600 font-medium">{selectedTicket?.title}</p>
          <div>
            <label className="block font-semibold uppercase text-slate-600 mb-1">Select Support Agent *</label>
            <select
              value={agentId}
              onChange={(e) => setAgentId(e.target.value)}
              className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg text-xs"
            >
              <option value="">Choose an agent...</option>
              {agents.map((ag) => (
                <option key={ag.id} value={ag.id}>
                  {ag.fullName} ({ag.role.toLowerCase()})
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block font-semibold uppercase text-slate-600 mb-1">Routing Team</label>
            <select
              value={teamId}
              onChange={(e) => setTeamId(e.target.value)}
              className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg text-xs"
            >
              <option value="">No team routing</option>
              {teams.map((tm) => (
                <option key={tm.id} value={tm.id}>
                  {tm.name}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block font-semibold uppercase text-slate-600 mb-1">Assignment Note</label>
            <textarea
              rows={2}
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              placeholder="e.g. Assigned based on expertise in network infrastructure"
              className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg text-xs"
            />
          </div>

          <div className="flex justify-end gap-2 pt-2">
            <button
              onClick={() => setSelectedTicket(null)}
              className="px-3 py-1.5 text-slate-600 hover:text-slate-800"
            >
              Cancel
            </button>
            <button
              onClick={handleAssign}
              disabled={!agentId}
              className="px-4 py-1.5 bg-sky-600 hover:bg-sky-700 text-white font-semibold rounded-lg disabled:opacity-40"
            >
              Confirm Assignment
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
