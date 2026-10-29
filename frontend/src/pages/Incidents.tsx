import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { incidentApi, authApi, ticketApi } from '../api/apiService';
import { Incident, User, Ticket } from '../api/types';
import { Modal } from '../components/Modal';
import { useAuth } from '../context/AuthContext';
import {
  Flame,
  Plus,
  Link as LinkIcon,
  CheckCircle2,
  AlertTriangle,
  ArrowRight,
  ShieldAlert,
  Server,
  FileText,
} from 'lucide-react';

export const Incidents: React.FC = () => {
  const navigate = useNavigate();
  const { isStaff } = useAuth();
  const [incidents, setIncidents] = useState<Incident[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [openTickets, setOpenTickets] = useState<Ticket[]>([]);
  const [loading, setLoading] = useState(true);

  // Declare Incident Modal
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [severity, setSeverity] = useState<'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW'>('CRITICAL');
  const [affectedService, setAffectedService] = useState('');
  const [ownerId, setOwnerId] = useState('');

  // Status/Resolution Modal
  const [selectedIncident, setSelectedIncident] = useState<Incident | null>(null);
  const [status, setStatus] = useState<string>('INVESTIGATING');
  const [rootCause, setRootCause] = useState('');
  const [resolutionSummary, setResolutionSummary] = useState('');

  // Link Ticket Modal
  const [linkingIncident, setLinkingIncident] = useState<Incident | null>(null);
  const [selectedTicketId, setSelectedTicketId] = useState('');

  const loadData = async () => {
    setLoading(true);
    try {
      const [incRes, userRes, tickRes] = await Promise.all([
        incidentApi.getIncidents(),
        authApi.getStaffAndAgents(),
        ticketApi.getTickets({ size: 100 }),
      ]);
      setIncidents(incRes);
      setUsers(userRes);
      setOpenTickets(tickRes.content.filter((t) => t.status !== 'RESOLVED' && t.status !== 'CLOSED'));
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleCreateIncident = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await incidentApi.createIncident({
        title,
        description,
        severity,
        affectedService,
        ownerId: ownerId ? Number(ownerId) : undefined,
      });
      setShowCreateModal(false);
      setTitle('');
      setDescription('');
      setAffectedService('');
      loadData();
    } catch (err: any) {
      alert(err.message || 'Failed to declare incident');
    }
  };

  const handleUpdateStatus = async () => {
    if (!selectedIncident) return;
    try {
      await incidentApi.updateStatus(selectedIncident.id, {
        status,
        rootCause,
        resolutionSummary,
      });
      setSelectedIncident(null);
      loadData();
    } catch (err: any) {
      alert(err.message || 'Failed to update incident');
    }
  };

  const handleLinkTicket = async () => {
    if (!linkingIncident || !selectedTicketId) return;
    try {
      await incidentApi.linkTickets(linkingIncident.id, [Number(selectedTicketId)]);
      setLinkingIncident(null);
      setSelectedTicketId('');
      loadData();
    } catch (err: any) {
      alert(err.message || 'Failed to link ticket');
    }
  };

  const getSeverityBadge = (sev: string) => {
    switch (sev) {
      case 'CRITICAL':
        return (
          <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-rose-100 text-rose-800 border border-rose-200 flex items-center gap-1">
            <Flame className="w-3.5 h-3.5 text-rose-600" /> Critical
          </span>
        );
      case 'HIGH':
        return (
          <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-100 text-amber-800 border border-amber-200">
            High
          </span>
        );
      case 'MEDIUM':
        return (
          <span className="px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800 border border-blue-200">
            Medium
          </span>
        );
      default:
        return (
          <span className="px-2.5 py-0.5 rounded-full text-xs font-medium bg-slate-100 text-slate-700">
            {sev}
          </span>
        );
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">Major Incident Management</h1>
          <p className="text-xs text-slate-500">
            Correlate related tickets, coordinate response teams, and maintain service health
          </p>
        </div>
        {isStaff && (
          <button
            onClick={() => setShowCreateModal(true)}
            className="inline-flex items-center gap-1.5 px-4 py-2 bg-rose-600 hover:bg-rose-700 text-white text-xs font-semibold rounded-lg shadow-sm transition-colors"
          >
            <Plus className="w-4 h-4" />
            <span>Declare Major Incident</span>
          </button>
        )}
      </div>

      <div className="grid grid-cols-1 gap-4">
        {loading ? (
          <div className="p-12 text-center bg-white rounded-xl border border-slate-200">
            <div className="w-6 h-6 border-3 border-sky-600 border-t-transparent rounded-full animate-spin mx-auto" />
          </div>
        ) : incidents.length === 0 ? (
          <div className="p-12 text-center bg-white rounded-xl border border-slate-200 text-slate-400">
            <CheckCircle2 className="w-10 h-10 mx-auto text-emerald-500 mb-2" />
            <p className="text-sm font-semibold text-slate-700">All Systems Operational</p>
            <p className="text-xs mt-1">No active major incidents or service disruptions reported</p>
          </div>
        ) : (
          incidents.map((inc) => (
            <div
              key={inc.id}
              className="p-5 bg-white rounded-xl border border-slate-200 shadow-xs hover:border-slate-300 transition-all space-y-4"
            >
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-slate-100 pb-3">
                <div className="flex items-center gap-3">
                  <span className="font-mono text-xs font-bold text-rose-600 bg-rose-50 px-2 py-0.5 rounded border border-rose-100">
                    {inc.incidentNumber}
                  </span>
                  {getSeverityBadge(inc.severity)}
                  <span
                    className={`px-2.5 py-0.5 rounded-full text-xs font-semibold border ${
                      inc.status === 'RESOLVED'
                        ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                        : inc.status === 'IDENTIFIED'
                        ? 'bg-amber-50 text-amber-700 border-amber-200'
                        : 'bg-rose-50 text-rose-700 border-rose-200 animate-pulse'
                    }`}
                  >
                    {inc.status}
                  </span>
                </div>

                <div className="flex items-center gap-2">
                  {isStaff && (
                    <>
                      <button
                        onClick={() => {
                          setLinkingIncident(inc);
                        }}
                        className="px-2.5 py-1 text-xs font-semibold text-slate-700 bg-slate-100 hover:bg-slate-200 rounded-md transition-colors flex items-center gap-1"
                      >
                        <LinkIcon className="w-3.5 h-3.5" /> Link Ticket
                      </button>
                      <button
                        onClick={() => {
                          setSelectedIncident(inc);
                          setStatus(inc.status);
                          setRootCause(inc.rootCause || '');
                          setResolutionSummary(inc.resolutionSummary || '');
                        }}
                        className="px-2.5 py-1 text-xs font-semibold text-sky-700 bg-sky-50 hover:bg-sky-100 rounded-md border border-sky-200 transition-colors"
                      >
                        Update Incident
                      </button>
                    </>
                  )}
                </div>
              </div>

              <div>
                <h3 className="text-base font-bold text-slate-800">{inc.title}</h3>
                <p className="text-xs text-slate-600 mt-1">{inc.description || 'No detailed description'}</p>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-4 gap-4 p-3 bg-slate-50 rounded-lg text-xs">
                <div>
                  <span className="text-slate-400 block text-[10px] uppercase font-semibold">Affected Service</span>
                  <span className="font-semibold text-slate-800">{inc.affectedService}</span>
                </div>
                <div>
                  <span className="text-slate-400 block text-[10px] uppercase font-semibold">Incident Owner</span>
                  <span className="font-semibold text-slate-800">{inc.owner?.fullName || 'Unassigned'}</span>
                </div>
                <div>
                  <span className="text-slate-400 block text-[10px] uppercase font-semibold">Started At</span>
                  <span className="text-slate-700">{new Date(inc.startedAt).toLocaleString()}</span>
                </div>
                <div>
                  <span className="text-slate-400 block text-[10px] uppercase font-semibold">Linked Tickets</span>
                  <span className="font-semibold text-sky-600">
                    {inc.linkedTicketCount} correlated ticket{inc.linkedTicketCount === 1 ? '' : 's'}
                  </span>
                </div>
              </div>

              {inc.rootCause && (
                <div className="p-3 bg-blue-50 border border-blue-200 rounded-lg text-xs space-y-1">
                  <span className="font-bold text-blue-900 uppercase tracking-wider block">Root Cause Analysis</span>
                  <p className="text-blue-800">{inc.rootCause}</p>
                </div>
              )}

              {inc.resolutionSummary && (
                <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-lg text-xs space-y-1">
                  <span className="font-bold text-emerald-900 uppercase tracking-wider block">Remediation Steps</span>
                  <p className="text-emerald-800">{inc.resolutionSummary}</p>
                </div>
              )}
            </div>
          ))
        )}
      </div>

      {/* Declare Incident Modal */}
      <Modal isOpen={showCreateModal} onClose={() => setShowCreateModal(false)} title="Declare Major Incident">
        <form onSubmit={handleCreateIncident} className="space-y-4 text-xs">
          <div>
            <label className="block font-semibold uppercase text-slate-600 mb-1">Incident Title *</label>
            <input
              type="text"
              required
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g. Production Payment Gateway High Latency & Failures"
              className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block font-semibold uppercase text-slate-600 mb-1">Severity *</label>
              <select
                value={severity}
                onChange={(e) => setSeverity(e.target.value as any)}
                className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg"
              >
                <option value="CRITICAL">Critical (Total Service Outage)</option>
                <option value="HIGH">High (Degraded Performance)</option>
                <option value="MEDIUM">Medium (Internal Disruption)</option>
                <option value="LOW">Low (Minor Impact)</option>
              </select>
            </div>
            <div>
              <label className="block font-semibold uppercase text-slate-600 mb-1">Affected Service *</label>
              <input
                type="text"
                required
                value={affectedService}
                onChange={(e) => setAffectedService(e.target.value)}
                placeholder="e.g. Stripe Checkout Gateway"
                className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg"
              />
            </div>
          </div>

          <div>
            <label className="block font-semibold uppercase text-slate-600 mb-1">Incident Commander / Owner</label>
            <select
              value={ownerId}
              onChange={(e) => setOwnerId(e.target.value)}
              className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg"
            >
              <option value="">Select Commander...</option>
              {users.map((u) => (
                <option key={u.id} value={u.id}>
                  {u.fullName} ({u.role})
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block font-semibold uppercase text-slate-600 mb-1">Initial Investigation Scope</label>
            <textarea
              rows={3}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Provide context, monitoring alert links, and initial impact metrics..."
              className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg"
            />
          </div>

          <div className="flex justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={() => setShowCreateModal(false)}
              className="px-3 py-1.5 text-slate-600 hover:text-slate-800"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-1.5 bg-rose-600 hover:bg-rose-700 text-white font-semibold rounded-lg"
            >
              Declare Incident
            </button>
          </div>
        </form>
      </Modal>

      {/* Link Ticket Modal */}
      <Modal
        isOpen={!!linkingIncident}
        onClose={() => setLinkingIncident(null)}
        title={`Link Ticket to ${linkingIncident?.incidentNumber || ''}`}
      >
        <div className="space-y-4 text-xs">
          <p className="text-slate-600">
            Select an active ticket to correlate with this incident. The ticket will inherit the incident tracking context.
          </p>
          <div>
            <label className="block font-semibold uppercase text-slate-600 mb-1">Select Open Ticket *</label>
            <select
              value={selectedTicketId}
              onChange={(e) => setSelectedTicketId(e.target.value)}
              className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg"
            >
              <option value="">Choose ticket to correlate...</option>
              {openTickets.map((t) => (
                <option key={t.id} value={t.id}>
                  {t.ticketNumber} · {t.title} ({t.priority})
                </option>
              ))}
            </select>
          </div>
          <div className="flex justify-end gap-2 pt-2">
            <button
              onClick={() => setLinkingIncident(null)}
              className="px-3 py-1.5 text-slate-600 hover:text-slate-800"
            >
              Cancel
            </button>
            <button
              onClick={handleLinkTicket}
              disabled={!selectedTicketId}
              className="px-4 py-1.5 bg-sky-600 hover:bg-sky-700 text-white font-semibold rounded-lg disabled:opacity-40"
            >
              Link Ticket
            </button>
          </div>
        </div>
      </Modal>

      {/* Update Status Modal */}
      <Modal
        isOpen={!!selectedIncident}
        onClose={() => setSelectedIncident(null)}
        title={`Update Incident: ${selectedIncident?.incidentNumber || ''}`}
      >
        <div className="space-y-4 text-xs">
          <div>
            <label className="block font-semibold uppercase text-slate-600 mb-1">Current Lifecycle Status *</label>
            <select
              value={status}
              onChange={(e) => setStatus(e.target.value)}
              className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg"
            >
              <option value="INVESTIGATING">INVESTIGATING (Initial Triage)</option>
              <option value="IDENTIFIED">IDENTIFIED (Root Cause Discovered)</option>
              <option value="MONITORING">MONITORING (Fix Applied & Validating)</option>
              <option value="RESOLVED">RESOLVED (Full Restoration)</option>
            </select>
          </div>

          <div>
            <label className="block font-semibold uppercase text-slate-600 mb-1">Root Cause Summary</label>
            <textarea
              rows={3}
              value={rootCause}
              onChange={(e) => setRootCause(e.target.value)}
              placeholder="Technical root cause analysis..."
              className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg"
            />
          </div>

          <div>
            <label className="block font-semibold uppercase text-slate-600 mb-1">Resolution Summary</label>
            <textarea
              rows={3}
              value={resolutionSummary}
              onChange={(e) => setResolutionSummary(e.target.value)}
              placeholder="Corrective actions performed to restore service..."
              className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg"
            />
          </div>

          <div className="flex justify-end gap-2 pt-2">
            <button
              onClick={() => setSelectedIncident(null)}
              className="px-3 py-1.5 text-slate-600 hover:text-slate-800"
            >
              Cancel
            </button>
            <button
              onClick={handleUpdateStatus}
              className="px-4 py-1.5 bg-sky-600 hover:bg-sky-700 text-white font-semibold rounded-lg"
            >
              Save Incident Update
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
