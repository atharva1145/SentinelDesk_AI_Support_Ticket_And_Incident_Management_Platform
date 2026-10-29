import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { dashboardApi, incidentApi, ticketApi } from '../api/apiService';
import { DashboardCharts, DashboardSummary, Incident, Ticket } from '../api/types';
import { PriorityBadge } from '../components/PriorityBadge';
import { StatusBadge } from '../components/StatusBadge';
import { SlaBadge } from '../components/SlaBadge';
import {
  Inbox,
  AlertTriangle,
  Flame,
  Clock,
  CheckCircle,
  TrendingUp,
  ArrowRight,
  ShieldAlert,
  Users,
} from 'lucide-react';
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  Tooltip,
  PieChart,
  Pie,
  Cell,
  BarChart,
  Bar,
} from 'recharts';

const PRIORITY_COLORS: Record<string, string> = {
  P1: '#f43f5e',
  P2: '#f59e0b',
  P3: '#3b82f6',
  P4: '#94a3b8',
};

export const Dashboard: React.FC = () => {
  const navigate = useNavigate();
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [charts, setCharts] = useState<DashboardCharts | null>(null);
  const [recentTickets, setRecentTickets] = useState<Ticket[]>([]);
  const [activeIncidents, setActiveIncidents] = useState<Incident[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadData() {
      try {
        const [sumRes, chartRes, tickRes, incRes] = await Promise.all([
          dashboardApi.getSummary(),
          dashboardApi.getCharts(),
          ticketApi.getTickets({ size: 5, sortBy: 'createdAt', sortDirection: 'desc' }),
          incidentApi.getIncidents(),
        ]);
        setSummary(sumRes);
        setCharts(chartRes);
        setRecentTickets(tickRes.content);
        setActiveIncidents(incRes.filter((i) => i.status !== 'RESOLVED'));
      } catch (err) {
        console.error('Failed to load dashboard:', err);
      } finally {
        setLoading(false);
      }
    }
    loadData();
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="w-8 h-8 border-4 border-sky-600 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Top Banner if Active Major Incidents */}
      {activeIncidents.length > 0 && (
        <div className="p-4 bg-rose-50 border border-rose-200 rounded-xl flex items-center justify-between shadow-xs animate-fade-in">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-rose-600 text-white rounded-lg animate-pulse">
              <Flame className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-sm font-bold text-rose-900">
                Active Major Incident: {activeIncidents[0].incidentNumber} — {activeIncidents[0].title}
              </h4>
              <p className="text-xs text-rose-700">
                Service Affected: <span className="font-semibold">{activeIncidents[0].affectedService}</span> · Status:{' '}
                <span className="font-semibold">{activeIncidents[0].status}</span>
              </p>
            </div>
          </div>
          <button
            onClick={() => navigate('/incidents')}
            className="px-3 py-1.5 bg-rose-600 hover:bg-rose-700 text-white text-xs font-semibold rounded-lg shadow-sm transition-colors"
          >
            Incident Command
          </button>
        </div>
      )}

      {/* KPI Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Open Tickets */}
        <div
          onClick={() => navigate('/tickets')}
          className="p-5 bg-white rounded-xl border border-slate-200 shadow-xs hover:border-slate-300 cursor-pointer transition-all"
        >
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">Total Open Tickets</span>
            <div className="p-2 bg-sky-50 text-sky-600 rounded-lg">
              <Inbox className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-2xl font-bold text-slate-900">{summary?.totalOpenTickets ?? 0}</span>
            <span className="text-xs text-slate-500">active items</span>
          </div>
        </div>

        {/* Unassigned Tickets */}
        <div
          onClick={() => navigate('/assignment-queue')}
          className="p-5 bg-white rounded-xl border border-slate-200 shadow-xs hover:border-slate-300 cursor-pointer transition-all"
        >
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">Unassigned Queue</span>
            <div className="p-2 bg-amber-50 text-amber-600 rounded-lg">
              <AlertTriangle className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-2xl font-bold text-slate-900">{summary?.unassignedTickets ?? 0}</span>
            <span className="text-xs text-amber-600 font-medium">pending agent assignment</span>
          </div>
        </div>

        {/* Critical & High P1/P2 */}
        <div
          onClick={() => navigate('/tickets?priority=P1')}
          className="p-5 bg-white rounded-xl border border-slate-200 shadow-xs hover:border-slate-300 cursor-pointer transition-all"
        >
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">Critical & High (P1/P2)</span>
            <div className="p-2 bg-rose-50 text-rose-600 rounded-lg">
              <ShieldAlert className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-2xl font-bold text-rose-600">{summary?.criticalAndHighTickets ?? 0}</span>
            <span className="text-xs text-slate-500">urgent SLA focus</span>
          </div>
        </div>

        {/* SLA Compliance */}
        <div
          onClick={() => navigate('/sla')}
          className="p-5 bg-white rounded-xl border border-slate-200 shadow-xs hover:border-slate-300 cursor-pointer transition-all"
        >
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">SLA Compliance</span>
            <div className="p-2 bg-emerald-50 text-emerald-600 rounded-lg">
              <CheckCircle className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-2xl font-bold text-emerald-600">{summary?.slaComplianceRatePercent ?? 100}%</span>
            <span className="text-xs text-slate-500">
              {summary?.slaBreachCount ?? 0} breach{(summary?.slaBreachCount ?? 0) === 1 ? '' : 'es'}
            </span>
          </div>
        </div>
      </div>

      {/* Secondary Performance Metrics */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-xs flex items-center gap-4">
          <div className="p-3 bg-blue-50 text-blue-600 rounded-xl">
            <Clock className="w-5 h-5" />
          </div>
          <div>
            <span className="text-xs text-slate-500 block">Avg First Response Time</span>
            <span className="text-lg font-bold text-slate-800">
              {summary?.avgFirstResponseMinutes ? `${summary.avgFirstResponseMinutes} mins` : '—'}
            </span>
          </div>
        </div>

        <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-xs flex items-center gap-4">
          <div className="p-3 bg-indigo-50 text-indigo-600 rounded-xl">
            <TrendingUp className="w-5 h-5" />
          </div>
          <div>
            <span className="text-xs text-slate-500 block">Avg Resolution Target</span>
            <span className="text-lg font-bold text-slate-800">
              {summary?.avgResolutionHours ? `${summary.avgResolutionHours} hrs` : '—'}
            </span>
          </div>
        </div>

        <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-xs flex items-center gap-4">
          <div className="p-3 bg-emerald-50 text-emerald-600 rounded-xl">
            <CheckCircle className="w-5 h-5" />
          </div>
          <div>
            <span className="text-xs text-slate-500 block">Resolved Today</span>
            <span className="text-lg font-bold text-slate-800">{summary?.ticketsResolvedToday ?? 0}</span>
          </div>
        </div>
      </div>

      {/* Charts Section */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Ticket Volume 7-Day Trend */}
        <div className="lg:col-span-2 p-5 bg-white rounded-xl border border-slate-200 shadow-xs space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-bold text-slate-800 uppercase tracking-wider">Ticket Volume (Last 7 Days)</h3>
            <span className="text-xs text-slate-400">Total tickets created per day</span>
          </div>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={charts?.volumeOverTime || []}>
                <defs>
                  <linearGradient id="colorTickets" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#0284c7" stopOpacity={0.4} />
                    <stop offset="95%" stopColor="#0284c7" stopOpacity={0.0} />
                  </linearGradient>
                </defs>
                <XAxis dataKey="date" stroke="#94a3b8" fontSize={11} />
                <YAxis allowDecimals={false} stroke="#94a3b8" fontSize={11} />
                <Tooltip />
                <Area
                  type="monotone"
                  dataKey="tickets"
                  stroke="#0284c7"
                  strokeWidth={2}
                  fillOpacity={1}
                  fill="url(#colorTickets)"
                />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* By Priority Pie Chart */}
        <div className="p-5 bg-white rounded-xl border border-slate-200 shadow-xs space-y-4">
          <h3 className="text-sm font-bold text-slate-800 uppercase tracking-wider">Tickets by Priority</h3>
          <div className="h-48 flex items-center justify-center">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={charts?.ticketsByPriority || []}
                  dataKey="count"
                  nameKey="name"
                  cx="50%"
                  cy="50%"
                  outerRadius={70}
                  innerRadius={45}
                  paddingAngle={3}
                >
                  {(charts?.ticketsByPriority || []).map((entry) => (
                    <Cell key={entry.name} fill={PRIORITY_COLORS[entry.name] || '#94a3b8'} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
          <div className="grid grid-cols-2 gap-2 text-xs">
            {(charts?.ticketsByPriority || []).map((entry) => (
              <div key={entry.name} className="flex items-center gap-2">
                <span
                  className="w-2.5 h-2.5 rounded-full"
                  style={{ backgroundColor: PRIORITY_COLORS[entry.name] || '#94a3b8' }}
                />
                <span className="text-slate-600 font-medium">
                  {entry.name}: {entry.count}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Category and Team Workload Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Category Breakdown */}
        <div className="p-5 bg-white rounded-xl border border-slate-200 shadow-xs space-y-4">
          <h3 className="text-sm font-bold text-slate-800 uppercase tracking-wider">Tickets by Category</h3>
          <div className="h-56">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={charts?.ticketsByCategory || []}>
                <XAxis dataKey="name" stroke="#94a3b8" fontSize={10} interval={0} angle={-15} textAnchor="end" />
                <YAxis allowDecimals={false} stroke="#94a3b8" fontSize={11} />
                <Tooltip />
                <Bar dataKey="count" fill="#3b82f6" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Team Workload */}
        <div className="p-5 bg-white rounded-xl border border-slate-200 shadow-xs space-y-4">
          <h3 className="text-sm font-bold text-slate-800 uppercase tracking-wider">Support Team Workload</h3>
          <div className="h-56">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={charts?.teamWorkload || []} layout="vertical">
                <XAxis type="number" allowDecimals={false} stroke="#94a3b8" fontSize={11} />
                <YAxis dataKey="teamName" type="category" stroke="#94a3b8" fontSize={11} width={130} />
                <Tooltip />
                <Bar dataKey="activeTickets" fill="#0ea5e9" radius={[0, 4, 4, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      {/* Recent Activity Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="p-5 border-b border-slate-100 flex items-center justify-between">
          <div>
            <h3 className="text-sm font-bold text-slate-800 uppercase tracking-wider">Recent Tickets</h3>
            <p className="text-xs text-slate-400">Recently logged tickets with live SLA tracking</p>
          </div>
          <button
            onClick={() => navigate('/tickets')}
            className="inline-flex items-center gap-1 text-xs font-semibold text-sky-600 hover:text-sky-700"
          >
            <span>View All</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </button>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 text-slate-500 text-xs font-semibold uppercase tracking-wider border-b border-slate-100">
              <tr>
                <th className="py-3 px-4">Ticket</th>
                <th className="py-3 px-4">Priority</th>
                <th className="py-3 px-4">Status</th>
                <th className="py-3 px-4">Requester</th>
                <th className="py-3 px-4">Assignee</th>
                <th className="py-3 px-4">SLA Deadline</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-slate-700">
              {recentTickets.map((t) => (
                <tr
                  key={t.id}
                  onClick={() => navigate(`/tickets/${t.id}`)}
                  className="hover:bg-slate-50/80 cursor-pointer transition-colors"
                >
                  <td className="py-3 px-4">
                    <span className="font-mono text-xs font-bold text-sky-600 block">{t.ticketNumber}</span>
                    <span className="font-medium text-slate-800 line-clamp-1">{t.title}</span>
                  </td>
                  <td className="py-3 px-4 whitespace-nowrap">
                    <PriorityBadge priority={t.priority} />
                  </td>
                  <td className="py-3 px-4 whitespace-nowrap">
                    <StatusBadge status={t.status} />
                  </td>
                  <td className="py-3 px-4 whitespace-nowrap text-xs text-slate-600">
                    {t.requester.fullName}
                  </td>
                  <td className="py-3 px-4 whitespace-nowrap text-xs text-slate-600">
                    {t.assignedAgent?.fullName || <span className="text-slate-400 italic">Unassigned</span>}
                  </td>
                  <td className="py-3 px-4 whitespace-nowrap">
                    <SlaBadge tracker={t.slaTracker} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
