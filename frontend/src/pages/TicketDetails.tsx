import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ticketApi, commentApi, aiApi, teamApi, authApi } from '../api/apiService';
import {
  Ticket,
  TicketComment,
  TicketStatus,
  TicketPriority,
  TicketCategory,
  Team,
  User,
  AiTriageSuggestion,
} from '../api/types';
import { PriorityBadge } from '../components/PriorityBadge';
import { StatusBadge } from '../components/StatusBadge';
import { SlaBadge } from '../components/SlaBadge';
import { Modal } from '../components/Modal';
import { useAuth } from '../context/AuthContext';
import {
  Clock,
  Sparkles,
  Send,
  Lock,
  MessageSquare,
  User as UserIcon,
  CheckCircle2,
  AlertCircle,
  HelpCircle,
  RotateCcw,
  ArrowRight,
  ShieldAlert,
  Flame,
  Check,
} from 'lucide-react';

export const TicketDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const ticketId = Number(id);
  const navigate = useNavigate();
  const { user, isStaff, isManager, isAdmin, isRequester } = useAuth();

  const [ticket, setTicket] = useState<Ticket | null>(null);
  const [comments, setComments] = useState<TicketComment[]>([]);
  const [statusHistory, setStatusHistory] = useState<any[]>([]);
  const [assignmentHistory, setAssignmentHistory] = useState<any[]>([]);
  const [agents, setAgents] = useState<User[]>([]);
  const [teams, setTeams] = useState<Team[]>([]);

  const [loading, setLoading] = useState(true);
  const [commentText, setCommentText] = useState('');
  const [isInternalComment, setIsInternalComment] = useState(false);
  const [submittingComment, setSubmittingComment] = useState(false);

  // Modals
  const [showAssignModal, setShowAssignModal] = useState(false);
  const [selectedAgentId, setSelectedAgentId] = useState<string>('');
  const [selectedTeamId, setSelectedTeamId] = useState<string>('');
  const [assignReason, setAssignReason] = useState('');

  const [showResolveModal, setShowResolveModal] = useState(false);
  const [resolutionSummary, setResolutionSummary] = useState('');

  const [showOverrideModal, setShowOverrideModal] = useState(false);
  const [overrideCategory, setOverrideCategory] = useState<TicketCategory>('SOFTWARE');
  const [overridePriority, setOverridePriority] = useState<TicketPriority>('P3');
  const [overrideTeamId, setOverrideTeamId] = useState<string>('');
  const [overrideReason, setOverrideReason] = useState('');

  const [showPriorityModal, setShowPriorityModal] = useState(false);
  const [newPriority, setNewPriority] = useState<TicketPriority>('P2');
  const [priorityChangeReason, setPriorityChangeReason] = useState('');

  const loadTicketData = async () => {
    try {
      const [tRes, cRes, sHist, aHist] = await Promise.all([
        ticketApi.getTicket(ticketId),
        commentApi.getComments(ticketId),
        ticketApi.getStatusHistory(ticketId),
        ticketApi.getAssignmentHistory(ticketId),
      ]);
      setTicket(tRes);
      setComments(cRes);
      setStatusHistory(sHist);
      setAssignmentHistory(aHist);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTicketData();
    if (isStaff) {
      authApi.getStaffAndAgents().then(setAgents).catch(console.error);
      teamApi.getTeams().then(setTeams).catch(console.error);
    }
  }, [ticketId]);

  const handlePostComment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!commentText.trim()) return;

    setSubmittingComment(true);
    try {
      await commentApi.addComment(ticketId, {
        content: commentText.trim(),
        isInternal: isStaff ? isInternalComment : false,
      });
      setCommentText('');
      setIsInternalComment(false);
      loadTicketData();
    } catch (err: any) {
      alert(err.message || 'Failed to submit comment');
    } finally {
      setSubmittingComment(false);
    }
  };

  const handleStatusTransition = async (newStatus: TicketStatus, summary?: string) => {
    try {
      await ticketApi.changeStatus(ticketId, {
        status: newStatus,
        reason: `Status updated to ${newStatus} by ${user?.fullName}`,
        resolutionSummary: summary,
      });
      loadTicketData();
    } catch (err: any) {
      alert(err.message || 'Status transition rejected');
    }
  };

  const handleConfirmAssign = async () => {
    try {
      await ticketApi.assignTicket(ticketId, {
        agentId: selectedAgentId ? Number(selectedAgentId) : null,
        teamId: selectedTeamId ? Number(selectedTeamId) : undefined,
        reason: assignReason || 'Assigned via web portal',
      });
      setShowAssignModal(false);
      loadTicketData();
    } catch (err: any) {
      alert(err.message || 'Assignment failed');
    }
  };

  const handleConfirmResolve = async () => {
    if (!resolutionSummary.trim()) {
      alert('Resolution summary is required to close out the ticket.');
      return;
    }
    await handleStatusTransition('RESOLVED', resolutionSummary.trim());
    setShowResolveModal(false);
    setResolutionSummary('');
  };

  const handleAcceptAi = async (suggId: number) => {
    try {
      await aiApi.acceptSuggestion(ticketId, suggId);
      loadTicketData();
    } catch (err: any) {
      alert(err.message || 'Failed to accept recommendation');
    }
  };

  const handleConfirmOverrideAi = async () => {
    if (!overrideReason.trim()) {
      alert('Override reason is mandatory for human auditing.');
      return;
    }
    if (!ticket?.aiSuggestion) return;

    try {
      await aiApi.overrideSuggestion(ticketId, ticket.aiSuggestion.id, {
        newCategory: overrideCategory,
        newPriority: overridePriority,
        newTeamId: overrideTeamId ? Number(overrideTeamId) : undefined,
        reason: overrideReason.trim(),
      });
      setShowOverrideModal(false);
      loadTicketData();
    } catch (err: any) {
      alert(err.message || 'Failed to record override');
    }
  };

  const handleConfirmPriorityChange = async () => {
    if (!priorityChangeReason.trim()) {
      alert('Reason for priority change is required.');
      return;
    }
    try {
      await ticketApi.changePriority(ticketId, {
        priority: newPriority,
        reason: priorityChangeReason.trim(),
      });
      setShowPriorityModal(false);
      loadTicketData();
    } catch (err: any) {
      alert(err.message || 'Priority change failed');
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="w-8 h-8 border-4 border-sky-600 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (!ticket) {
    return (
      <div className="p-12 text-center text-slate-500">
        <p className="text-base font-semibold">Ticket not found or permission denied</p>
        <button
          onClick={() => navigate('/tickets')}
          className="mt-4 px-4 py-2 bg-sky-600 text-white rounded-lg text-xs"
        >
          Return to Tickets
        </button>
      </div>
    );
  }

  // Determine valid next workflow states
  const validTransitions: { status: TicketStatus; label: string; action: () => void }[] = [];
  if (ticket.status === 'NEW') {
    validTransitions.push({
      status: 'OPEN',
      label: 'Open Ticket',
      action: () => handleStatusTransition('OPEN'),
    });
    validTransitions.push({
      status: 'TRIAGED',
      label: 'Mark Triaged',
      action: () => handleStatusTransition('TRIAGED'),
    });
  } else if (ticket.status === 'TRIAGED') {
    validTransitions.push({
      status: 'OPEN',
      label: 'Open Ticket',
      action: () => handleStatusTransition('OPEN'),
    });
    validTransitions.push({
      status: 'IN_PROGRESS',
      label: 'Start Progress',
      action: () => handleStatusTransition('IN_PROGRESS'),
    });
  } else if (ticket.status === 'OPEN') {
    validTransitions.push({
      status: 'IN_PROGRESS',
      label: 'Start Progress',
      action: () => handleStatusTransition('IN_PROGRESS'),
    });
    validTransitions.push({
      status: 'PENDING_REQUESTER',
      label: 'Awaiting Requester Info',
      action: () => handleStatusTransition('PENDING_REQUESTER'),
    });
    validTransitions.push({
      status: 'RESOLVED',
      label: 'Mark Resolved',
      action: () => setShowResolveModal(true),
    });
  } else if (ticket.status === 'IN_PROGRESS') {
    validTransitions.push({
      status: 'PENDING_REQUESTER',
      label: 'Awaiting Requester Info',
      action: () => handleStatusTransition('PENDING_REQUESTER'),
    });
    validTransitions.push({
      status: 'RESOLVED',
      label: 'Mark Resolved',
      action: () => setShowResolveModal(true),
    });
  } else if (ticket.status === 'PENDING_REQUESTER') {
    validTransitions.push({
      status: 'IN_PROGRESS',
      label: 'Resume In Progress',
      action: () => handleStatusTransition('IN_PROGRESS'),
    });
    validTransitions.push({
      status: 'RESOLVED',
      label: 'Mark Resolved',
      action: () => setShowResolveModal(true),
    });
  } else if (ticket.status === 'RESOLVED') {
    validTransitions.push({
      status: 'CLOSED',
      label: 'Close Ticket',
      action: () => handleStatusTransition('CLOSED'),
    });
    validTransitions.push({
      status: 'OPEN',
      label: 'Reopen Ticket',
      action: () => handleStatusTransition('OPEN'),
    });
  }

  return (
    <div className="space-y-6">
      {/* Top Header Card */}
      <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-xs space-y-4">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <span className="font-mono text-sm font-bold text-sky-600 bg-sky-50 px-2.5 py-0.5 rounded border border-sky-100">
                {ticket.ticketNumber}
              </span>
              <PriorityBadge priority={ticket.priority} />
              <StatusBadge status={ticket.status} />
              <SlaBadge tracker={ticket.slaTracker} />
            </div>
            <h1 className="text-xl font-bold text-slate-900 tracking-tight">{ticket.title}</h1>
            <p className="text-xs text-slate-500">
              Submitted by <span className="font-semibold text-slate-700">{ticket.requester.fullName}</span> ({ticket.requester.department || 'General'}) on{' '}
              {new Date(ticket.createdAt).toLocaleString()} · Category:{' '}
              <span className="font-medium text-slate-800 capitalize">{ticket.category.toLowerCase()}</span>
            </p>
          </div>

          {/* Quick Actions Bar */}
          <div className="flex flex-wrap items-center gap-2">
            {isStaff && (
              <button
                onClick={() => {
                  setSelectedAgentId(ticket.assignedAgent ? String(ticket.assignedAgent.id) : '');
                  setSelectedTeamId(ticket.assignedTeam ? String(ticket.assignedTeam.id) : '');
                  setShowAssignModal(true);
                }}
                className="px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold rounded-lg transition-colors"
              >
                Assign / Reroute
              </button>
            )}

            {isManager && (
              <button
                onClick={() => {
                  setNewPriority(ticket.priority);
                  setShowPriorityModal(true);
                }}
                className="px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold rounded-lg transition-colors"
              >
                Change Priority
              </button>
            )}

            {validTransitions.map((vt) => (
              <button
                key={vt.status}
                onClick={vt.action}
                className="px-3.5 py-1.5 bg-sky-600 hover:bg-sky-700 text-white text-xs font-semibold rounded-lg shadow-sm transition-colors"
              >
                {vt.label}
              </button>
            ))}
          </div>
        </div>

        {ticket.relatedIncident && (
          <div className="p-3 bg-rose-50 border border-rose-200 rounded-lg flex items-center justify-between text-xs text-rose-800">
            <div className="flex items-center gap-2">
              <Flame className="w-4 h-4 text-rose-600 shrink-0" />
              <span>
                Linked to Major Incident: <span className="font-bold">{ticket.relatedIncident.incidentNumber}</span> — {ticket.relatedIncident.title}
              </span>
            </div>
            <button
              onClick={() => navigate('/incidents')}
              className="text-rose-700 font-semibold underline hover:text-rose-900"
            >
              View Incident
            </button>
          </div>
        )}

        {ticket.resolutionSummary && (
          <div className="p-4 bg-emerald-50 border border-emerald-200 rounded-lg text-xs space-y-1">
            <span className="font-bold text-emerald-900 uppercase tracking-wider block flex items-center gap-1.5">
              <CheckCircle2 className="w-4 h-4 text-emerald-600" /> Resolution Summary
            </span>
            <p className="text-emerald-800">{ticket.resolutionSummary}</p>
          </div>
        )}
      </div>

      {/* Main 2-Column Body */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column (2 Cols): Description & Conversation */}
        <div className="lg:col-span-2 space-y-6">
          {/* Ticket Description */}
          <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-xs space-y-3">
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-500">Problem Description</h3>
            <div className="p-4 bg-slate-50 rounded-lg text-sm text-slate-800 font-mono whitespace-pre-wrap leading-relaxed border border-slate-100">
              {ticket.description}
            </div>
          </div>

          {/* Conversation and Notes Feed */}
          <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
            <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <MessageSquare className="w-4 h-4 text-slate-500" />
                <h3 className="text-sm font-bold text-slate-800 uppercase tracking-wider">
                  Ticket Discussion & Activity
                </h3>
              </div>
              <span className="text-xs text-slate-400">{comments.length} message{comments.length === 1 ? '' : 's'}</span>
            </div>

            {/* Comments List */}
            <div className="p-6 space-y-4">
              {comments.length === 0 ? (
                <p className="text-xs text-slate-400 italic text-center py-4">No comments or updates recorded yet.</p>
              ) : (
                comments.map((c) => (
                  <div
                    key={c.id}
                    className={`p-4 rounded-xl border text-sm transition-all ${
                      c.isInternal
                        ? 'bg-amber-50/60 border-amber-200/70'
                        : 'bg-slate-50/80 border-slate-200'
                    }`}
                  >
                    <div className="flex items-center justify-between mb-2">
                      <div className="flex items-center gap-2">
                        <span className="font-semibold text-slate-800 text-xs">{c.author.fullName}</span>
                        <span className="text-[10px] text-slate-400 capitalize">({c.author.role.toLowerCase()})</span>
                        {c.isInternal && (
                          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-200/80 text-amber-900">
                            <Lock className="w-2.5 h-2.5" /> Internal Staff Note
                          </span>
                        )}
                      </div>
                      <span className="text-[11px] text-slate-400">
                        {new Date(c.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} ·{' '}
                        {new Date(c.createdAt).toLocaleDateString()}
                      </span>
                    </div>
                    <p className="text-slate-800 whitespace-pre-wrap text-xs leading-relaxed">{c.content}</p>
                  </div>
                ))
              )}

              {/* Add Comment Box */}
              <form onSubmit={handlePostComment} className="pt-4 border-t border-slate-100 space-y-3">
                <textarea
                  rows={3}
                  value={commentText}
                  onChange={(e) => setCommentText(e.target.value)}
                  placeholder={
                    isInternalComment
                      ? 'Write a private note visible only to support staff and administrators...'
                      : 'Post a public reply visible to the requester...'
                  }
                  className={`w-full p-3 rounded-lg border text-xs focus:outline-hidden focus:ring-2 transition-all ${
                    isInternalComment
                      ? 'bg-amber-50/40 border-amber-200 focus:ring-amber-500/20 focus:border-amber-500'
                      : 'bg-slate-50 border-slate-200 focus:ring-sky-500/20 focus:border-sky-500'
                  }`}
                />

                <div className="flex items-center justify-between">
                  {isStaff ? (
                    <label className="flex items-center gap-2 text-xs font-semibold text-amber-900 cursor-pointer">
                      <input
                        type="checkbox"
                        checked={isInternalComment}
                        onChange={(e) => setIsInternalComment(e.target.checked)}
                        className="w-4 h-4 rounded text-amber-600 border-amber-300 focus:ring-amber-500"
                      />
                      <span className="flex items-center gap-1">
                        <Lock className="w-3.5 h-3.5 text-amber-600" /> Private Internal Note
                      </span>
                    </label>
                  ) : <div />}

                  <button
                    type="submit"
                    disabled={submittingComment || !commentText.trim()}
                    className={`inline-flex items-center gap-1.5 px-4 py-2 rounded-lg text-xs font-semibold text-white transition-all shadow-sm disabled:opacity-40 ${
                      isInternalComment ? 'bg-amber-600 hover:bg-amber-700' : 'bg-sky-600 hover:bg-sky-700'
                    }`}
                  >
                    <Send className="w-3.5 h-3.5" />
                    <span>{submittingComment ? 'Sending...' : isInternalComment ? 'Post Note' : 'Reply Publicly'}</span>
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>

        {/* Right Column: AI Triage, SLA Timer, Metadata & History */}
        <div className="space-y-6">
          {/* AI Triage Card */}
          {ticket.aiSuggestion ? (
            <div className="bg-gradient-to-br from-white to-sky-50/40 rounded-xl border border-sky-200 p-5 shadow-xs space-y-4">
              <div className="flex items-center justify-between border-b border-sky-100 pb-3">
                <div className="flex items-center gap-2">
                  <div className="p-1.5 bg-sky-600 text-white rounded-md">
                    <Sparkles className="w-4 h-4" />
                  </div>
                  <div>
                    <h3 className="text-xs font-bold text-sky-950 uppercase tracking-wider">AI Triage Recommendation</h3>
                    <span className="text-[10px] text-sky-600 font-medium">{ticket.aiSuggestion.modelProvider} · {ticket.aiSuggestion.confidenceEstimate}% confidence</span>
                  </div>
                </div>

                {ticket.aiSuggestion.isAccepted && (
                  <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-100 text-emerald-800 border border-emerald-200">
                    Accepted
                  </span>
                )}
                {ticket.aiSuggestion.isOverridden && (
                  <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-100 text-amber-800 border border-amber-200">
                    Overridden
                  </span>
                )}
              </div>

              {/* Suggestions Data */}
              <div className="space-y-2 text-xs">
                <div>
                  <span className="text-slate-400 block text-[10px] uppercase font-semibold">Suggested Routing</span>
                  <p className="font-semibold text-slate-800">{ticket.aiSuggestion.suggestedTeam} ({ticket.aiSuggestion.suggestedDomain})</p>
                </div>

                <div>
                  <span className="text-slate-400 block text-[10px] uppercase font-semibold">Priority Assessment</span>
                  <div className="flex items-center gap-2 mt-0.5">
                    <PriorityBadge priority={ticket.aiSuggestion.suggestedPriority} />
                    <span className="text-[11px] text-slate-600">{ticket.aiSuggestion.priorityExplanation}</span>
                  </div>
                </div>

                {ticket.aiSuggestion.diagnosticQuestions?.length > 0 && (
                  <div>
                    <span className="text-slate-400 block text-[10px] uppercase font-semibold">Diagnostic Questions</span>
                    <ul className="list-disc list-inside space-y-0.5 mt-1 text-[11px] text-slate-700">
                      {ticket.aiSuggestion.diagnosticQuestions.map((q, idx) => (
                        <li key={idx}>{q}</li>
                      ))}
                    </ul>
                  </div>
                )}

                {ticket.aiSuggestion.suggestedActions?.length > 0 && (
                  <div>
                    <span className="text-slate-400 block text-[10px] uppercase font-semibold">Recommended Actions</span>
                    <ul className="list-disc list-inside space-y-0.5 mt-1 text-[11px] text-slate-700">
                      {ticket.aiSuggestion.suggestedActions.map((a, idx) => (
                        <li key={idx}>{a}</li>
                      ))}
                    </ul>
                  </div>
                )}

                {ticket.aiSuggestion.isOverridden && (
                  <div className="p-2.5 bg-amber-50 rounded-lg border border-amber-200 text-[11px] text-amber-900 space-y-0.5">
                    <span className="font-bold block">Human Override Reason:</span>
                    <p>{ticket.aiSuggestion.humanOverrideReason}</p>
                    <span className="text-[10px] text-amber-700 block">By: {ticket.aiSuggestion.overriddenBy?.fullName}</span>
                  </div>
                )}
              </div>

              {/* Action Buttons */}
              {isStaff && !ticket.aiSuggestion.isAccepted && (
                <div className="flex items-center gap-2 pt-2 border-t border-sky-100">
                  <button
                    onClick={() => handleAcceptAi(ticket.aiSuggestion!.id)}
                    className="flex-1 py-1.5 bg-sky-600 hover:bg-sky-700 text-white text-xs font-semibold rounded-lg shadow-sm transition-colors text-center"
                  >
                    Accept Suggestion
                  </button>
                  <button
                    onClick={() => {
                      setOverrideCategory(ticket.aiSuggestion!.suggestedCategory);
                      setOverridePriority(ticket.aiSuggestion!.suggestedPriority);
                      setShowOverrideModal(true);
                    }}
                    className="py-1.5 px-3 bg-white hover:bg-slate-50 text-slate-700 border border-slate-300 text-xs font-semibold rounded-lg transition-colors"
                  >
                    Override
                  </button>
                </div>
              )}
            </div>
          ) : (
            <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-xs text-center text-slate-400 text-xs">
              <Sparkles className="w-5 h-5 mx-auto mb-1 text-slate-300" />
              <span>AI triage not generated for this ticket</span>
            </div>
          )}

          {/* Ticket Metadata Card */}
          <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-xs space-y-3">
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-500 border-b border-slate-100 pb-2">
              Assignment & Metadata
            </h3>
            <div className="space-y-3 text-xs">
              <div className="flex justify-between items-center">
                <span className="text-slate-400 font-medium">Requester:</span>
                <span className="font-semibold text-slate-800">{ticket.requester.fullName}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-slate-400 font-medium">Department:</span>
                <span className="font-semibold text-slate-800">{ticket.requester.department || 'N/A'}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-slate-400 font-medium">Assigned Agent:</span>
                <span className="font-semibold text-slate-800">
                  {ticket.assignedAgent ? ticket.assignedAgent.fullName : <span className="text-amber-600">Unassigned</span>}
                </span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-slate-400 font-medium">Assigned Team:</span>
                <span className="font-semibold text-slate-800">
                  {ticket.assignedTeam ? ticket.assignedTeam.name : '—'}
                </span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-slate-400 font-medium">First Response:</span>
                <span className="font-semibold text-slate-800">
                  {ticket.firstResponseAt ? new Date(ticket.firstResponseAt).toLocaleTimeString() : 'Awaiting initial response'}
                </span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-slate-400 font-medium">SLA Deadline:</span>
                <span className="font-semibold text-slate-800">
                  {ticket.slaDeadline ? new Date(ticket.slaDeadline).toLocaleString() : '—'}
                </span>
              </div>
            </div>
          </div>

          {/* Status Transition History Timeline */}
          <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-xs space-y-3">
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-500 border-b border-slate-100 pb-2">
              Audit History
            </h3>
            <div className="space-y-3 text-xs max-h-56 overflow-y-auto">
              {statusHistory.map((sh, idx) => (
                <div key={idx} className="flex items-start gap-2 text-[11px]">
                  <div className="w-1.5 h-1.5 rounded-full bg-sky-500 mt-1.5 shrink-0" />
                  <div>
                    <span className="font-semibold text-slate-800">
                      {sh.oldStatus ? `${sh.oldStatus} → ` : ''}{sh.newStatus}
                    </span>
                    <p className="text-slate-500">{sh.reason}</p>
                    <span className="text-[10px] text-slate-400">
                      {new Date(sh.createdAt).toLocaleString()} · {sh.changedBy?.fullName || 'System'}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Reassign Modal */}
      <Modal isOpen={showAssignModal} onClose={() => setShowAssignModal(false)} title="Assign Ticket">
        <div className="space-y-4 text-xs">
          <div>
            <label className="block font-semibold uppercase text-slate-600 mb-1">Select Support Agent</label>
            <select
              value={selectedAgentId}
              onChange={(e) => setSelectedAgentId(e.target.value)}
              className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg text-xs"
            >
              <option value="">Leave unassigned (Team Queue)</option>
              {agents.map((ag) => (
                <option key={ag.id} value={ag.id}>
                  {ag.fullName} ({ag.role.toLowerCase()})
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block font-semibold uppercase text-slate-600 mb-1">Select Responsible Team</label>
            <select
              value={selectedTeamId}
              onChange={(e) => setSelectedTeamId(e.target.value)}
              className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg text-xs"
            >
              <option value="">No team assignment</option>
              {teams.map((tm) => (
                <option key={tm.id} value={tm.id}>
                  {tm.name}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block font-semibold uppercase text-slate-600 mb-1">Assignment Reason / Handover Note</label>
            <textarea
              rows={2}
              value={assignReason}
              onChange={(e) => setAssignReason(e.target.value)}
              placeholder="e.g. Escalating to Network Tier 2 for packet capture inspection"
              className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg text-xs"
            />
          </div>

          <div className="flex justify-end gap-2 pt-2">
            <button
              onClick={() => setShowAssignModal(false)}
              className="px-3 py-1.5 text-slate-600 hover:text-slate-800"
            >
              Cancel
            </button>
            <button
              onClick={handleConfirmAssign}
              className="px-4 py-1.5 bg-sky-600 hover:bg-sky-700 text-white font-semibold rounded-lg"
            >
              Confirm Assignment
            </button>
          </div>
        </div>
      </Modal>

      {/* Resolve Modal */}
      <Modal isOpen={showResolveModal} onClose={() => setShowResolveModal(false)} title="Resolve Support Ticket">
        <div className="space-y-4 text-xs">
          <p className="text-slate-600">
            Please document the root cause and steps taken to remediate this issue for the requester and knowledge base.
          </p>
          <div>
            <label className="block font-semibold uppercase text-slate-600 mb-1">Resolution Summary *</label>
            <textarea
              required
              rows={4}
              value={resolutionSummary}
              onChange={(e) => setResolutionSummary(e.target.value)}
              placeholder="Describe what was fixed, configuration updated, or verified with the user..."
              className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg text-xs"
            />
          </div>
          <div className="flex justify-end gap-2 pt-2">
            <button
              onClick={() => setShowResolveModal(false)}
              className="px-3 py-1.5 text-slate-600 hover:text-slate-800"
            >
              Cancel
            </button>
            <button
              onClick={handleConfirmResolve}
              className="px-4 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold rounded-lg"
            >
              Resolve Ticket
            </button>
          </div>
        </div>
      </Modal>

      {/* Override AI Modal */}
      <Modal isOpen={showOverrideModal} onClose={() => setShowOverrideModal(false)} title="Override AI Recommendation">
        <div className="space-y-4 text-xs">
          <p className="text-slate-600">
            Override the automated AI categorization or priority. Human override rationale is audited in system logs.
          </p>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block font-semibold uppercase text-slate-600 mb-1">New Category *</label>
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
              <label className="block font-semibold uppercase text-slate-600 mb-1">New Priority *</label>
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
              placeholder="Explain why the AI suggestion was modified (e.g. Verified customer is on an isolated VPN subnet, reducing severity from P1 to P2)"
              className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg"
            />
          </div>

          <div className="flex justify-end gap-2 pt-2">
            <button
              onClick={() => setShowOverrideModal(false)}
              className="px-3 py-1.5 text-slate-600 hover:text-slate-800"
            >
              Cancel
            </button>
            <button
              onClick={handleConfirmOverrideAi}
              className="px-4 py-1.5 bg-amber-600 hover:bg-amber-700 text-white font-semibold rounded-lg"
            >
              Save Override
            </button>
          </div>
        </div>
      </Modal>

      {/* Priority Change Modal */}
      <Modal isOpen={showPriorityModal} onClose={() => setShowPriorityModal(false)} title="Change Priority Level">
        <div className="space-y-4 text-xs">
          <div>
            <label className="block font-semibold uppercase text-slate-600 mb-1">New Priority *</label>
            <select
              value={newPriority}
              onChange={(e) => setNewPriority(e.target.value as TicketPriority)}
              className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg"
            >
              <option value="P1">P1 · Critical (15 min response, 4h resolution)</option>
              <option value="P2">P2 · High (1h response, 8h resolution)</option>
              <option value="P3">P3 · Medium (4h response, 48h resolution)</option>
              <option value="P4">P4 · Low (24h response, 120h resolution)</option>
            </select>
          </div>
          <div>
            <label className="block font-semibold uppercase text-slate-600 mb-1">Mandatory Reason *</label>
            <textarea
              required
              rows={3}
              value={priorityChangeReason}
              onChange={(e) => setPriorityChangeReason(e.target.value)}
              placeholder="e.g. Escalated due to end-of-quarter revenue cycle impact"
              className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg"
            />
          </div>
          <div className="flex justify-end gap-2 pt-2">
            <button
              onClick={() => setShowPriorityModal(false)}
              className="px-3 py-1.5 text-slate-600 hover:text-slate-800"
            >
              Cancel
            </button>
            <button
              onClick={handleConfirmPriorityChange}
              className="px-4 py-1.5 bg-sky-600 hover:bg-sky-700 text-white font-semibold rounded-lg"
            >
              Update Priority & SLA
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
