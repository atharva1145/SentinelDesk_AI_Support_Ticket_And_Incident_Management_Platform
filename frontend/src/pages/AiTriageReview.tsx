import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ticketApi, aiApi } from '../api/apiService';
import { Ticket, TicketCategory, TicketPriority } from '../api/types';
import { PriorityBadge } from '../components/PriorityBadge';
import { Modal } from '../components/Modal';
import { Sparkles, CheckCircle2, AlertOctagon, HelpCircle, ArrowRight, Check, X } from 'lucide-react';

export const AiTriageReview: React.FC = () => {
  const navigate = useNavigate();
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [loading, setLoading] = useState(true);

  const [selectedTicket, setSelectedTicket] = useState<Ticket | null>(null);
  const [overrideCategory, setOverrideCategory] = useState<TicketCategory>('SOFTWARE');
  const [overridePriority, setOverridePriority] = useState<TicketPriority>('P3');
  const [overrideReason, setOverrideReason] = useState('');

  const loadTriageQueue = async () => {
    setLoading(true);
    try {
      const res = await ticketApi.getTickets({ size: 50, sortBy: 'createdAt', sortDirection: 'desc' });
      // Filter to tickets that have AI suggestions
      const withAi = res.content.filter((t) => t.aiSuggestion);
      setTickets(withAi);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTriageQueue();
  }, []);

  const handleAccept = async (t: Ticket) => {
    if (!t.aiSuggestion) return;
    try {
      await aiApi.acceptSuggestion(t.id, t.aiSuggestion.id);
      loadTriageQueue();
    } catch (err: any) {
      alert(err.message || 'Failed to accept AI suggestion');
    }
  };

  const handleConfirmOverride = async () => {
    if (!selectedTicket || !selectedTicket.aiSuggestion) return;
    if (!overrideReason.trim()) {
      alert('Override justification is required');
      return;
    }
    try {
      await aiApi.overrideSuggestion(selectedTicket.id, selectedTicket.aiSuggestion.id, {
        newCategory: overrideCategory,
        newPriority: overridePriority,
        reason: overrideReason.trim(),
      });
      setSelectedTicket(null);
      setOverrideReason('');
      loadTriageQueue();
    } catch (err: any) {
      alert(err.message || 'Failed to record override');
    }
  };

  // Stats calculation
  const totalClassified = tickets.length;
  const acceptedCount = tickets.filter((t) => t.aiSuggestion?.isAccepted).length;
  const overriddenCount = tickets.filter((t) => t.aiSuggestion?.isOverridden).length;
  const pendingCount = totalClassified - acceptedCount - overriddenCount;
  const acceptanceRate = totalClassified > 0 ? Math.round((acceptedCount / (acceptedCount + overriddenCount || 1)) * 100) : 100;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">AI Triage Audit & Review</h1>
          <p className="text-xs text-slate-500">
            Review machine learning classifications, confidence scores, and record human overrides
          </p>
        </div>
        <div className="p-2 bg-sky-50 text-sky-700 rounded-lg border border-sky-200 text-xs font-semibold flex items-center gap-2">
          <Sparkles className="w-4 h-4 text-sky-600" />
          <span>Active Classifier: OpenAI / Fallback Rules</span>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
        <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-xs">
          <span className="text-xs text-slate-500 font-semibold uppercase">Total Classified</span>
          <p className="text-2xl font-bold text-slate-800 mt-1">{totalClassified}</p>
        </div>
        <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-xs">
          <span className="text-xs text-slate-500 font-semibold uppercase">Pending Human Review</span>
          <p className="text-2xl font-bold text-amber-600 mt-1">{pendingCount}</p>
        </div>
        <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-xs">
          <span className="text-xs text-slate-500 font-semibold uppercase">Human Overrides</span>
          <p className="text-2xl font-bold text-purple-600 mt-1">{overriddenCount}</p>
        </div>
        <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-xs">
          <span className="text-xs text-slate-500 font-semibold uppercase">AI Recommendation Acceptance</span>
          <p className="text-2xl font-bold text-emerald-600 mt-1">{acceptanceRate}%</p>
        </div>
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
        {loading ? (
          <div className="p-12 text-center">
            <div className="w-6 h-6 border-3 border-sky-600 border-t-transparent rounded-full animate-spin mx-auto" />
          </div>
        ) : tickets.length === 0 ? (
          <div className="p-12 text-center text-slate-400">
            <Sparkles className="w-8 h-8 mx-auto text-slate-300 mb-2" />
            <p className="text-sm font-semibold text-slate-700">No AI-triaged tickets found</p>
            <p className="text-xs mt-1">Submit tickets to view automated classifications</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-slate-500 text-xs font-semibold uppercase tracking-wider border-b border-slate-100">
                <tr>
                  <th className="py-3 px-4">Ticket</th>
                  <th className="py-3 px-4">AI Suggested Priority</th>
                  <th className="py-3 px-4">AI Category</th>
                  <th className="py-3 px-4">Suggested Routing</th>
                  <th className="py-3 px-4">Confidence</th>
                  <th className="py-3 px-4">Review Status</th>
                  <th className="py-3 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-slate-700">
                {tickets.map((t) => {
                  const s = t.aiSuggestion!;
                  return (
                    <tr key={t.id} className="hover:bg-slate-50/80 transition-colors">
                      <td
                        onClick={() => navigate(`/tickets/${t.id}`)}
                        className="py-3.5 px-4 cursor-pointer max-w-xs"
                      >
                        <span className="font-mono text-xs font-bold text-sky-600 block">{t.ticketNumber}</span>
                        <span className="font-medium text-slate-800 line-clamp-1">{t.title}</span>
                      </td>
                      <td className="py-3.5 px-4 whitespace-nowrap">
                        <PriorityBadge priority={s.suggestedPriority} />
                      </td>
                      <td className="py-3.5 px-4 whitespace-nowrap text-xs text-slate-600 font-medium capitalize">
                        {s.suggestedCategory.toLowerCase()}
                      </td>
                      <td className="py-3.5 px-4 whitespace-nowrap text-xs text-slate-600">
                        {s.suggestedTeam}
                      </td>
                      <td className="py-3.5 px-4 whitespace-nowrap">
                        <span className="text-xs font-bold text-slate-700">{s.confidenceEstimate}%</span>
                      </td>
                      <td className="py-3.5 px-4 whitespace-nowrap">
                        {s.isAccepted ? (
                          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-emerald-50 text-emerald-700 border border-emerald-200">
                            <CheckCircle2 className="w-3 h-3" /> Accepted
                          </span>
                        ) : s.isOverridden ? (
                          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-purple-50 text-purple-700 border border-purple-200">
                            <AlertOctagon className="w-3 h-3" /> Overridden
                          </span>
                        ) : (
                          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-amber-50 text-amber-700 border border-amber-200">
                            Pending Review
                          </span>
                        )}
                      </td>
                      <td className="py-3.5 px-4 text-right whitespace-nowrap">
                        <div className="flex items-center justify-end gap-1.5">
                          {!s.isAccepted && (
                            <button
                              onClick={() => handleAccept(t)}
                              title="Accept AI Recommendation"
                              className="p-1.5 bg-emerald-50 hover:bg-emerald-100 text-emerald-700 rounded-md border border-emerald-200"
                            >
                              <Check className="w-3.5 h-3.5" />
                            </button>
                          )}
                          <button
                            onClick={() => {
                              setSelectedTicket(t);
                              setOverrideCategory(s.suggestedCategory);
                              setOverridePriority(s.suggestedPriority);
                            }}
                            title="Override with Human Rationale"
                            className="px-2.5 py-1 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold rounded-md border border-slate-200"
                          >
                            Override
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Override Modal */}
      <Modal
        isOpen={!!selectedTicket}
        onClose={() => setSelectedTicket(null)}
        title={`Override AI Triage for ${selectedTicket?.ticketNumber || ''}`}
      >
        <div className="space-y-4 text-xs">
          <p className="text-slate-600 font-medium">{selectedTicket?.title}</p>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block font-semibold uppercase text-slate-600 mb-1">Human Category *</label>
              <select
                value={overrideCategory}
                onChange={(e) => setOverrideCategory(e.target.value as TicketCategory)}
                className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg"
              >
                <option value="SOFTWARE">Software Issue</option>
                <option value="HARDWARE">Hardware Failure</option>
                <option value="NETWORK">Network Issue</option>
                <option value="ACCESS">Access & Identity</option>
                <option value="SECURITY">Security Incident</option>
                <option value="OUTAGE">Production Outage</option>
                <option value="OTHER">Other</option>
              </select>
            </div>
            <div>
              <label className="block font-semibold uppercase text-slate-600 mb-1">Human Priority *</label>
              <select
                value={overridePriority}
                onChange={(e) => setOverridePriority(e.target.value as TicketPriority)}
                className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg"
              >
                <option value="P1">P1 · Critical</option>
                <option value="P2">P2 · High</option>
                <option value="P3">P3 · Medium</option>
                <option value="P4">P4 · Low</option>
              </select>
            </div>
          </div>

          <div>
            <label className="block font-semibold uppercase text-slate-600 mb-1">Mandatory Override Reason *</label>
            <textarea
              required
              rows={3}
              value={overrideReason}
              onChange={(e) => setOverrideReason(e.target.value)}
              placeholder="Explain why the AI suggestion was overridden..."
              className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg"
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
              onClick={handleConfirmOverride}
              className="px-4 py-1.5 bg-purple-600 hover:bg-purple-700 text-white font-semibold rounded-lg"
            >
              Save Override & Update Ticket
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
