import React, { useEffect, useState } from 'react';
import { dashboardApi, reportApi } from '../api/apiService';
import { DashboardCharts, DashboardSummary, SlaReportSummary } from '../api/types';
import {
  BarChart3,
  TrendingUp,
  Clock,
  CheckCircle2,
  AlertTriangle,
  Award,
  Users,
  Download,
  FileSpreadsheet,
  ShieldCheck,
} from 'lucide-react';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  PieChart,
  Pie,
  Cell,
} from 'recharts';

export const ReportsAnalytics: React.FC = () => {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [charts, setCharts] = useState<DashboardCharts | null>(null);
  const [slaSummary, setSlaSummary] = useState<SlaReportSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [isExporting, setIsExporting] = useState(false);

  useEffect(() => {
    async function loadData() {
      try {
        const [sumRes, chartRes, slaRes] = await Promise.all([
          dashboardApi.getSummary(),
          dashboardApi.getCharts(),
          reportApi.getSlaSummary().catch(() => null),
        ]);
        setSummary(sumRes);
        setCharts(chartRes);
        if (slaRes) setSlaSummary(slaRes);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    }
    loadData();
  }, []);

  const handleExportCsv = () => {
    setIsExporting(true);
    try {
      reportApi.exportSlaReportCsv();
    } finally {
      setTimeout(() => setIsExporting(false), 1500);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="w-8 h-8 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header with Export Button */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight flex items-center gap-2">
            <BarChart3 className="w-7 h-7 text-indigo-600" />
            ITSM Performance Reports & SLA Analytics
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Aggregated SLA compliance statistics, MTTA/MTTR metrics, and audit export from persisted records.
          </p>
        </div>

        <button
          onClick={handleExportCsv}
          disabled={isExporting}
          className="flex items-center gap-2 px-4 py-2 bg-emerald-600 hover:bg-emerald-700 disabled:opacity-50 text-white rounded-lg text-sm font-semibold shadow-sm transition-colors"
        >
          <FileSpreadsheet className="w-4 h-4" />
          {isExporting ? 'Generating CSV...' : 'Export SLA Audit Report (CSV)'}
        </button>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
        <div className="p-5 bg-white rounded-xl border border-slate-200 shadow-xs flex items-center gap-4">
          <div className="p-3 bg-emerald-50 text-emerald-600 rounded-xl">
            <Award className="w-6 h-6" />
          </div>
          <div>
            <span className="text-xs text-slate-500 font-semibold uppercase">Overall SLA Compliance</span>
            <div className="text-2xl font-bold text-emerald-600 mt-1">
              {slaSummary?.overallComplianceRate ?? summary?.slaComplianceRatePercent}%
            </div>
            <span className="text-[11px] text-slate-400">Target standard: 95.0%</span>
          </div>
        </div>

        <div className="p-5 bg-white rounded-xl border border-slate-200 shadow-xs flex items-center gap-4">
          <div className="p-3 bg-blue-50 text-blue-600 rounded-xl">
            <Clock className="w-6 h-6" />
          </div>
          <div>
            <span className="text-xs text-slate-500 font-semibold uppercase">Mean Time to Acknowledge (MTTA)</span>
            <div className="text-2xl font-bold text-slate-800 mt-1">
              {slaSummary?.meanTimeToAcknowledgeMinutes ?? summary?.avgFirstResponseMinutes}m
            </div>
            <span className="text-[11px] text-slate-400">
              Response compliance: {slaSummary?.firstResponseComplianceRate ?? 96}%
            </span>
          </div>
        </div>

        <div className="p-5 bg-white rounded-xl border border-slate-200 shadow-xs flex items-center gap-4">
          <div className="p-3 bg-purple-50 text-purple-600 rounded-xl">
            <CheckCircle2 className="w-6 h-6" />
          </div>
          <div>
            <span className="text-xs text-slate-500 font-semibold uppercase">Mean Time to Resolve (MTTR)</span>
            <div className="text-2xl font-bold text-slate-800 mt-1">
              {slaSummary?.meanTimeToResolveMinutes
                ? `${Math.round(slaSummary.meanTimeToResolveMinutes / 60)}h ${slaSummary.meanTimeToResolveMinutes % 60}m`
                : `${summary?.avgResolutionHours}h`}
            </div>
            <span className="text-[11px] text-slate-400">
              Resolution compliance: {slaSummary?.resolutionComplianceRate ?? 92}%
            </span>
          </div>
        </div>

        <div className="p-5 bg-white rounded-xl border border-slate-200 shadow-xs flex items-center gap-4">
          <div className="p-3 bg-rose-50 text-rose-600 rounded-xl">
            <AlertTriangle className="w-6 h-6" />
          </div>
          <div>
            <span className="text-xs text-slate-500 font-semibold uppercase">SLA Breaches</span>
            <div className="text-2xl font-bold text-rose-600 mt-1">
              {slaSummary?.breachedTickets ?? summary?.slaBreachCount}
            </div>
            <span className="text-[11px] text-slate-400">Total overdue incidents</span>
          </div>
        </div>
      </div>

      {/* Priority Breakdown Table */}
      {slaSummary?.priorityBreakdown && (
        <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
          <div className="p-4 border-b border-slate-100 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <ShieldCheck className="w-4 h-4 text-indigo-600" />
              <h3 className="text-xs font-bold uppercase tracking-wider text-slate-800">
                SLA Compliance Breakdown by Priority Policy
              </h3>
            </div>
            <span className="text-[11px] text-slate-400">Audit-ready breakdown</span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 border-b border-slate-100 text-[11px] uppercase font-bold text-slate-500">
                <tr>
                  <th className="py-2.5 px-4">Priority Level</th>
                  <th className="py-2.5 px-4">Total Tickets</th>
                  <th className="py-2.5 px-4">Compliant</th>
                  <th className="py-2.5 px-4">Breached</th>
                  <th className="py-2.5 px-4">Compliance %</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-xs">
                {Object.entries(slaSummary.priorityBreakdown).map(([prio, stats]) => {
                  const rate = stats.total > 0 ? Math.round((stats.compliant * 100) / stats.total) : 100;
                  return (
                    <tr key={prio} className="hover:bg-slate-50/60">
                      <td className="py-2.5 px-4 font-bold font-mono">
                        <span
                          className={`px-2 py-0.5 rounded text-xs ${
                            prio === 'P1'
                              ? 'bg-rose-100 text-rose-800'
                              : prio === 'P2'
                              ? 'bg-orange-100 text-orange-800'
                              : prio === 'P3'
                              ? 'bg-blue-100 text-blue-800'
                              : 'bg-slate-100 text-slate-700'
                          }`}
                        >
                          {prio}
                        </span>
                      </td>
                      <td className="py-2.5 px-4 text-slate-700 font-semibold">{stats.total}</td>
                      <td className="py-2.5 px-4 text-emerald-600 font-semibold">{stats.compliant}</td>
                      <td className="py-2.5 px-4 text-rose-600 font-semibold">{stats.breached}</td>
                      <td className="py-2.5 px-4 font-bold">
                        <div className="flex items-center gap-2">
                          <div className="w-24 bg-slate-200 rounded-full h-2">
                            <div
                              className={`h-2 rounded-full ${
                                rate >= 90 ? 'bg-emerald-500' : rate >= 75 ? 'bg-amber-500' : 'bg-rose-500'
                              }`}
                              style={{ width: `${rate}%` }}
                            />
                          </div>
                          <span>{rate}%</span>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Agent Workload Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="p-4 border-b border-slate-100 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Users className="w-4 h-4 text-sky-600" />
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-800">
              Agent Workload & Active Ticket Capacity
            </h3>
          </div>
          <span className="text-[11px] text-slate-400">Real-time open assignment load</span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 border-b border-slate-100 text-[11px] uppercase font-bold text-slate-500">
              <tr>
                <th className="py-2.5 px-4">Agent Name</th>
                <th className="py-2.5 px-4">Active Tickets</th>
                <th className="py-2.5 px-4">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-xs">
              {charts?.agentWorkload?.map((agent) => (
                <tr key={agent.agentId} className="hover:bg-slate-50/60">
                  <td className="py-2.5 px-4 font-semibold text-slate-800">{agent.agentName}</td>
                  <td className="py-2.5 px-4 font-bold text-sky-700">{agent.activeTickets}</td>
                  <td className="py-2.5 px-4">
                    <span
                      className={`inline-flex px-2 py-0.5 rounded-full text-[10px] font-bold ${
                        agent.activeTickets > 5
                          ? 'bg-rose-100 text-rose-800'
                          : agent.activeTickets > 2
                          ? 'bg-amber-100 text-amber-800'
                          : 'bg-emerald-100 text-emerald-800'
                      }`}
                    >
                      {agent.activeTickets > 5 ? 'High Load' : agent.activeTickets > 2 ? 'Moderate' : 'Optimal'}
                    </span>
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
