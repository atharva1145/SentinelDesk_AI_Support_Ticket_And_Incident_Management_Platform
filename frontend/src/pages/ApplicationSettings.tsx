import React from 'react';
import { Settings, Cpu, ShieldCheck, Clock, Server, CheckCircle2 } from 'lucide-react';

export const ApplicationSettings: React.FC = () => {
  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div>
        <h1 className="text-xl font-bold text-slate-900 tracking-tight">System & Application Settings</h1>
        <p className="text-xs text-slate-500">
          Environment configuration, AI triage parameters, and SLA engine scheduler status
        </p>
      </div>

      {/* Backend & DB Health */}
      <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-xs space-y-4">
        <div className="flex items-center gap-3 border-b border-slate-100 pb-3">
          <div className="p-2 bg-emerald-50 text-emerald-600 rounded-lg">
            <Server className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-sm font-bold text-slate-800">Backend Infrastructure</h3>
            <p className="text-xs text-slate-400">Spring Boot 3.3.4 · Java 21 LTS · Flyway Migrations</p>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
          <div className="p-3 bg-slate-50 rounded-lg space-y-1">
            <span className="text-slate-400 block font-semibold text-[10px] uppercase">Service Health</span>
            <div className="flex items-center gap-1.5 text-emerald-600 font-bold">
              <CheckCircle2 className="w-4 h-4" /> Operational (Actuator UP)
            </div>
          </div>
          <div className="p-3 bg-slate-50 rounded-lg space-y-1">
            <span className="text-slate-400 block font-semibold text-[10px] uppercase">Database Engine</span>
            <span className="font-semibold text-slate-800">PostgreSQL 16 / Flyway V1-V2</span>
          </div>
        </div>
      </div>

      {/* AI Triage Configuration */}
      <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-xs space-y-4">
        <div className="flex items-center gap-3 border-b border-slate-100 pb-3">
          <div className="p-2 bg-sky-50 text-sky-600 rounded-lg">
            <Cpu className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-sm font-bold text-slate-800">AI Triage Classifier Configuration</h3>
            <p className="text-xs text-slate-400">Configured via environment variables (`AI_API_KEY`, `AI_MODEL`)</p>
          </div>
        </div>

        <div className="space-y-3 text-xs">
          <div className="flex justify-between items-center py-2 border-b border-slate-100">
            <span className="text-slate-600 font-medium">Provider Integration:</span>
            <span className="font-mono bg-slate-100 px-2 py-0.5 rounded text-slate-800">OpenAI / Gemini / Fallback</span>
          </div>
          <div className="flex justify-between items-center py-2 border-b border-slate-100">
            <span className="text-slate-600 font-medium">Structured Output Schema:</span>
            <span className="font-semibold text-emerald-600">Strict JSON Object Validated</span>
          </div>
          <div className="flex justify-between items-center py-2 border-b border-slate-100">
            <span className="text-slate-600 font-medium">Failure Tolerance:</span>
            <span className="font-semibold text-emerald-600">Deterministic Rule-Engine Graceful Fallback</span>
          </div>
          <div className="flex justify-between items-center py-2">
            <span className="text-slate-600 font-medium">Safety Constraint:</span>
            <span className="text-slate-700">Autonomous ticket closure blocked; Human overrides audited</span>
          </div>
        </div>
      </div>

      {/* SLA Engine Scheduler */}
      <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-xs space-y-4">
        <div className="flex items-center gap-3 border-b border-slate-100 pb-3">
          <div className="p-2 bg-amber-50 text-amber-600 rounded-lg">
            <Clock className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-sm font-bold text-slate-800">SLA Engine Background Scheduler</h3>
            <p className="text-xs text-slate-400">Continuous deadline tracking and idempotent escalation triggering</p>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
          <div className="p-3 bg-slate-50 rounded-lg space-y-1">
            <span className="text-slate-400 block font-semibold text-[10px] uppercase">Check Frequency</span>
            <span className="font-bold text-slate-800">30 seconds (fixedDelay)</span>
          </div>
          <div className="p-3 bg-slate-50 rounded-lg space-y-1">
            <span className="text-slate-400 block font-semibold text-[10px] uppercase">Warning Threshold</span>
            <span className="font-bold text-slate-800">80% of SLA window elapsed</span>
          </div>
        </div>
      </div>
    </div>
  );
};
