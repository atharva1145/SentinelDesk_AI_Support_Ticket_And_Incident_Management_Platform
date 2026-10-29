import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { ticketApi, teamApi } from '../api/apiService';
import { Ticket, TicketStatus, TicketPriority, TicketCategory, Team } from '../api/types';
import { PriorityBadge } from '../components/PriorityBadge';
import { StatusBadge } from '../components/StatusBadge';
import { SlaBadge } from '../components/SlaBadge';
import { Plus, Search, Filter, ChevronLeft, ChevronRight, CheckSquare } from 'lucide-react';

export const AllTickets: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [teams, setTeams] = useState<Team[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const [query, setQuery] = useState(searchParams.get('query') || '');
  const [status, setStatus] = useState<string>(searchParams.get('status') || '');
  const [priority, setPriority] = useState<string>(searchParams.get('priority') || '');
  const [category, setCategory] = useState<string>(searchParams.get('category') || '');
  const [teamId, setTeamId] = useState<string>(searchParams.get('teamId') || '');
  const [unassigned, setUnassigned] = useState(searchParams.get('unassigned') === 'true');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    teamApi.getTeams().then(setTeams).catch(console.error);
  }, []);

  const fetchTickets = async () => {
    setLoading(true);
    try {
      const res = await ticketApi.getTickets({
        query: query || undefined,
        status: (status as TicketStatus) || undefined,
        priority: (priority as TicketPriority) || undefined,
        category: (category as TicketCategory) || undefined,
        teamId: teamId ? Number(teamId) : undefined,
        unassigned: unassigned ? true : undefined,
        page,
        size: 15,
        sortBy: 'createdAt',
        sortDirection: 'desc',
      });
      setTickets(res.content);
      setTotalElements(res.totalElements);
      setTotalPages(res.totalPages);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTickets();
  }, [page, status, priority, category, teamId, unassigned]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    fetchTickets();
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">All Support Tickets</h1>
          <p className="text-xs text-slate-500">
            System-wide incident and support request catalog across all departments
          </p>
        </div>
        <button
          onClick={() => navigate('/tickets/new')}
          className="inline-flex items-center gap-1.5 px-4 py-2 bg-sky-600 hover:bg-sky-700 text-white text-xs font-semibold rounded-lg shadow-sm transition-colors"
        >
          <Plus className="w-4 h-4" />
          <span>New Ticket</span>
        </button>
      </div>

      {/* Advanced Filter Bar */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs space-y-3">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <form onSubmit={handleSearch} className="relative flex-1 min-w-[240px] max-w-md">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search by ticket #, subject, or description..."
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              className="w-full pl-9 pr-3 py-1.5 bg-slate-50 border border-slate-200 rounded-lg text-xs text-slate-800 placeholder-slate-400 focus:outline-hidden focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500"
            />
          </form>

          <label className="flex items-center gap-2 text-xs font-medium text-slate-700 cursor-pointer select-none">
            <input
              type="checkbox"
              checked={unassigned}
              onChange={(e) => {
                setUnassigned(e.target.checked);
                setPage(0);
              }}
              className="w-4 h-4 rounded text-sky-600 border-slate-300 focus:ring-sky-500"
            />
            <span>Unassigned only</span>
          </label>
        </div>

        {/* Dropdowns */}
        <div className="flex flex-wrap items-center gap-2 pt-2 border-t border-slate-100">
          <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider flex items-center gap-1">
            <Filter className="w-3 h-3" /> Filters:
          </span>

          <select
            value={status}
            onChange={(e) => {
              setStatus(e.target.value);
              setPage(0);
            }}
            className="text-xs bg-slate-50 border border-slate-200 rounded-lg px-2.5 py-1 text-slate-700 focus:outline-hidden"
          >
            <option value="">All Statuses</option>
            <option value="NEW">New</option>
            <option value="TRIAGED">Triaged</option>
            <option value="OPEN">Open</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="PENDING_REQUESTER">Pending Requester</option>
            <option value="RESOLVED">Resolved</option>
            <option value="CLOSED">Closed</option>
            <option value="CANCELLED">Cancelled</option>
          </select>

          <select
            value={priority}
            onChange={(e) => {
              setPriority(e.target.value);
              setPage(0);
            }}
            className="text-xs bg-slate-50 border border-slate-200 rounded-lg px-2.5 py-1 text-slate-700 focus:outline-hidden"
          >
            <option value="">All Priorities</option>
            <option value="P1">P1 · Critical</option>
            <option value="P2">P2 · High</option>
            <option value="P3">P3 · Medium</option>
            <option value="P4">P4 · Low</option>
          </select>

          <select
            value={category}
            onChange={(e) => {
              setCategory(e.target.value);
              setPage(0);
            }}
            className="text-xs bg-slate-50 border border-slate-200 rounded-lg px-2.5 py-1 text-slate-700 focus:outline-hidden"
          >
            <option value="">All Categories</option>
            <option value="OUTAGE">Outage</option>
            <option value="NETWORK">Network Issue</option>
            <option value="ACCESS">Access & Identity</option>
            <option value="SECURITY">Security Incident</option>
            <option value="SOFTWARE">Software Bug</option>
            <option value="HARDWARE">Hardware Issue</option>
            <option value="OTHER">Other</option>
          </select>

          <select
            value={teamId}
            onChange={(e) => {
              setTeamId(e.target.value);
              setPage(0);
            }}
            className="text-xs bg-slate-50 border border-slate-200 rounded-lg px-2.5 py-1 text-slate-700 focus:outline-hidden"
          >
            <option value="">All Teams</option>
            {teams.map((tm) => (
              <option key={tm.id} value={tm.id}>
                {tm.name}
              </option>
            ))}
          </select>

          {(status || priority || category || teamId || unassigned || query) && (
            <button
              onClick={() => {
                setStatus('');
                setPriority('');
                setCategory('');
                setTeamId('');
                setUnassigned(false);
                setQuery('');
                setPage(0);
              }}
              className="text-xs text-rose-600 hover:text-rose-700 font-medium ml-auto"
            >
              Reset Filters
            </button>
          )}
        </div>
      </div>

      {/* Ticket Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
        {loading ? (
          <div className="p-12 text-center">
            <div className="w-6 h-6 border-3 border-sky-600 border-t-transparent rounded-full animate-spin mx-auto" />
          </div>
        ) : tickets.length === 0 ? (
          <div className="p-12 text-center text-slate-400">
            <p className="text-sm font-medium">No tickets match the selected filters</p>
            <p className="text-xs mt-1">Try clearing or adjusting search filters above</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-slate-500 text-xs font-semibold uppercase tracking-wider border-b border-slate-100">
                <tr>
                  <th className="py-3 px-4">Ticket</th>
                  <th className="py-3 px-4">Priority</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4">Category</th>
                  <th className="py-3 px-4">Requester</th>
                  <th className="py-3 px-4">Assignee</th>
                  <th className="py-3 px-4">Team</th>
                  <th className="py-3 px-4">SLA Deadline</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-slate-700">
                {tickets.map((t) => (
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
                    <td className="py-3.5 px-4 whitespace-nowrap">
                      <StatusBadge status={t.status} />
                    </td>
                    <td className="py-3.5 px-4 whitespace-nowrap text-xs text-slate-600 font-medium capitalize">
                      {t.category.toLowerCase()}
                    </td>
                    <td className="py-3.5 px-4 whitespace-nowrap text-xs text-slate-600">
                      {t.requester.fullName}
                    </td>
                    <td className="py-3.5 px-4 whitespace-nowrap text-xs text-slate-600">
                      {t.assignedAgent?.fullName || <span className="text-amber-600 font-medium">Unassigned</span>}
                    </td>
                    <td className="py-3.5 px-4 whitespace-nowrap text-xs text-slate-500">
                      {t.assignedTeam?.name || '—'}
                    </td>
                    <td className="py-3.5 px-4 whitespace-nowrap">
                      <SlaBadge tracker={t.slaTracker} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination Bar */}
        <div className="p-4 border-t border-slate-100 flex items-center justify-between text-xs text-slate-500">
          <span>
            Total tickets: <span className="font-semibold text-slate-700">{totalElements}</span>
          </span>
          <div className="flex items-center gap-2">
            <button
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              disabled={page === 0}
              className="p-1 rounded-md border border-slate-200 text-slate-600 hover:bg-slate-50 disabled:opacity-40"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <span>
              Page {page + 1} of {Math.max(1, totalPages)}
            </span>
            <button
              onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
              disabled={page >= totalPages - 1}
              className="p-1 rounded-md border border-slate-200 text-slate-600 hover:bg-slate-50 disabled:opacity-40"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
