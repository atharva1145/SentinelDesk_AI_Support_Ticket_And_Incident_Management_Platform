import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ticketApi, teamApi } from '../api/apiService';
import { TicketCategory, TicketPriority, Team } from '../api/types';
import { Sparkles, ArrowRight, ShieldCheck, CheckCircle2, HelpCircle } from 'lucide-react';

export const CreateTicket: React.FC = () => {
  const navigate = useNavigate();
  const [teams, setTeams] = useState<Team[]>([]);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState<TicketCategory>('SOFTWARE');
  const [priority, setPriority] = useState<TicketPriority>('P3');
  const [assignedTeamId, setAssignedTeamId] = useState<string>('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  React.useEffect(() => {
    teamApi.getTeams().then(setTeams).catch(console.error);
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const res = await ticketApi.createTicket({
        title,
        description,
        category,
        priority,
        assignedTeamId: assignedTeamId ? Number(assignedTeamId) : undefined,
      });
      navigate(`/tickets/${res.id}`);
    } catch (err: any) {
      setError(err.message || 'Failed to submit support ticket');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div>
        <h1 className="text-xl font-bold text-slate-900 tracking-tight">Submit New Support Request</h1>
        <p className="text-xs text-slate-500">
          Provide complete details below. Automated AI triage and SLA timers will initialize immediately upon creation.
        </p>
      </div>

      <div className="bg-white rounded-xl border border-slate-200 shadow-xs p-6 space-y-6">
        {error && (
          <div className="p-3 bg-rose-50 border border-rose-200 rounded-lg text-xs text-rose-700">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-5">
          {/* Title */}
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-600 mb-1">
              Ticket Subject / Issue Summary *
            </label>
            <input
              type="text"
              required
              minLength={3}
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g. Cannot connect to staging Kubernetes API cluster"
              className="w-full px-3.5 py-2 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-900 placeholder-slate-400 focus:outline-hidden focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500 transition-all"
            />
          </div>

          {/* Category & Priority Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-600 mb-1">
                Category *
              </label>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value as TicketCategory)}
                className="w-full px-3.5 py-2 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-900 focus:outline-hidden focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500"
              >
                <option value="SOFTWARE">Software / Application Bug</option>
                <option value="HARDWARE">Hardware Failure</option>
                <option value="NETWORK">Network / VPN Issue</option>
                <option value="ACCESS">Access & Identity Request</option>
                <option value="SECURITY">Security Incident Alert</option>
                <option value="OUTAGE">Production Service Outage</option>
                <option value="OTHER">Other Technical Problem</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-600 mb-1">
                Urgency & Priority *
              </label>
              <select
                value={priority}
                onChange={(e) => setPriority(e.target.value as TicketPriority)}
                className="w-full px-3.5 py-2 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-900 focus:outline-hidden focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500"
              >
                <option value="P1">P1 · Critical Outage (15m response target)</option>
                <option value="P2">P2 · High Impact (1h response target)</option>
                <option value="P3">P3 · Medium (4h response target)</option>
                <option value="P4">P4 · Low / General Inquiry (24h target)</option>
              </select>
            </div>
          </div>

          {/* Assigned Team */}
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-600 mb-1">
              Direct Team Routing (Optional)
            </label>
            <select
              value={assignedTeamId}
              onChange={(e) => setAssignedTeamId(e.target.value)}
              className="w-full px-3.5 py-2 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-900 focus:outline-hidden focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500"
            >
              <option value="">Auto-assign via AI triage routing</option>
              {teams.map((tm) => (
                <option key={tm.id} value={tm.id}>
                  {tm.name} {tm.categoryFocus ? `(${tm.categoryFocus})` : ''}
                </option>
              ))}
            </select>
          </div>

          {/* Detailed Description */}
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-600 mb-1">
              Problem Description & Reproduction Steps *
            </label>
            <textarea
              required
              rows={6}
              minLength={10}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Describe what occurred, any error messages, affected systems, and timestamps..."
              className="w-full px-3.5 py-2 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-900 placeholder-slate-400 focus:outline-hidden focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500 transition-all font-mono text-xs"
            />
          </div>

          {/* AI Notice */}
          <div className="p-4 bg-sky-50/60 rounded-xl border border-sky-100 flex items-start gap-3">
            <Sparkles className="w-5 h-5 text-sky-600 shrink-0 mt-0.5" />
            <div className="text-xs text-slate-600 space-y-1">
              <p className="font-semibold text-sky-950">Automated AI Triage Active</p>
              <p>
                SentinelDesk AI will analyze this ticket's payload to verify priority, generate diagnostic questions for
                the technician, and route to the correct team. Human staff always maintain final review authority.
              </p>
            </div>
          </div>

          {/* Actions */}
          <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-100">
            <button
              type="button"
              onClick={() => navigate(-1)}
              className="px-4 py-2 text-xs font-semibold text-slate-600 hover:text-slate-800"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="inline-flex items-center gap-2 px-5 py-2.5 bg-sky-600 hover:bg-sky-700 text-white text-xs font-semibold rounded-lg shadow-sm transition-all disabled:opacity-50"
            >
              <span>{loading ? 'Submitting...' : 'Submit Support Ticket'}</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
