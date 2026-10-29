import React, { useEffect, useState } from 'react';
import { dashboardApi } from '../api/apiService';
import { DashboardCharts, DashboardSummary } from '../api/types';
import {
  BarChart3,
  TrendingUp,
  Clock,
  CheckCircle2,
  AlertTriangle,
  Award,
  Users,
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
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadData() {
      try {
        const [sumRes, chartRes] = await Promise.all([
          dashboardApi.getSummary(),
          dashboardApi.getCharts(),
        ]);
        setSummary(sumRes);
        setCharts(chartRes);
      } catch (err) {
        console.error(err);
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
      <div>
        <h1 className="text-xl font-bold text-slate-900 tracking-tight">ITSM Performance Reports & SLA Analytics</h1>
        <p className="text-xs text-slate-500">
          Aggregated SLA compliance statistics and team productivity benchmarks from persisted incident records
        </p>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="p-5 bg-white rounded-xl border border-slate-200 shadow-xs flex items-center gap-4">
          <div className="p-3 bg-emerald-50 text-emerald-600 rounded-xl">
            <Award className="w-6 h-6" />
          </div>
          <div>
            <span className="text-xs text-slate-500 font-semibold uppercase">Overall SLA Compliance</span>
            <div className="text-2xl font-bold text-emerald-600 mt-1">
              {summary?.slaComplianceRatePercent}%
            </div>
            <span className="text-[11px] text-slate-400">Target standard: 95.0%</span>
          </div>
        </div>

        <div className="p-5 bg-white rounded-xl border border-slate-200 shadow-xs flex items-center gap-4">
          <div className="p-3 bg-blue-50 text-blue-600 rounded-xl">
            <Clock className="w-6 h-6" />
          </div>
          <div>
            <span className="text-xs text-slate-500 font-semibold uppercase">Avg First Response Time</span>
            <div className="text-2xl font-bold text-slate-800 mt-1">
              {summary?.avgFirstResponseMinutes ? `${summary.avgFirstResponseMinutes}m` : '—'}
            </div>
            <span className="text-[11px] text-slate-400">Mean time to initial response</span>
          </div>
        </div>

        <div className="p-5 bg-white rounded-xl border border-slate-200 shadow-xs flex items-center gap-4">
          <div className="p-3 bg-purple-50 text-purple-600 rounded-xl">
            <CheckCircle2 className="w-6 h-6" />
          </div>
          <div>
            <span className="text-xs text-slate-500 font-semibold uppercase">Avg Mean Time to Resolve (MTTR)</span>
            <div className="text-2xl font-bold text-slate-800 mt-1">
              {summary?.avgResolutionHours ? `${summary.avgResolutionHours}h` : '—'}
            </div>
            <span className="text-[11px] text-slate-400">Across all ticket severities</span>
          </div>
        </div>
      </div>

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
            <thead className="bg-slate-50 text-slate-500 text-xs font-semibold uppercase tracking-wider border-b border-slate-100">
              <tr>
                <th className="py-3 px-4">Support Agent</th>
                <th className="py-3 px-4">Active Open Tickets</th>
                <th className="py-3 px-4">Capacity Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-slate-700">
              {(charts?.agentWorkload || []).map((ag) => (
                <tr key={ag.agentId} className="hover:bg-slate-50 transition-colors">
                  <td className="py-3.5 px-4 font-semibold text-slate-800">{ag.agentName}</td>
                  <td className="py-3.5 px-4 font-bold text-sky-600">{ag.activeTickets} tickets</td>
                  <td className="py-3.5 px-4">
                    {ag.activeTickets > 8 ? (
                      <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-rose-50 text-rose-700 border border-rose-200">
                        High Load
                      </span>
                    ) : ag.activeTickets > 4 ? (
                      <span className="px-2 py-0.5 rounded text-[10px] font-medium bg-amber-50 text-amber-700 border border-amber-200">
                        Balanced
                      </span>
                    ) : (
                      <span className="px-2 py-0.5 rounded text-[10px] font-medium bg-emerald-50 text-emerald-700 border border-emerald-200">
                        Optimal Available
                      </span>
                    )}
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
