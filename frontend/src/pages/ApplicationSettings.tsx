import React, { useState } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import {
  Settings,
  Cpu,
  ShieldCheck,
  Clock,
  Server,
  CheckCircle2,
  Mail,
  Send,
  Sparkles,
  Zap,
  AlertCircle,
  Radio,
} from 'lucide-react';
import { aiApi, emailApi } from '../api/apiService';
import { InboundEmailResponse } from '../api/types';

export const ApplicationSettings: React.FC = () => {
  // AI Test state
  const [testResult, setTestResult] = useState<any>(null);

  // Inbound Email Simulator state
  const [emailFrom, setEmailFrom] = useState('david.miller@external-client.com');
  const [emailName, setEmailName] = useState('David Miller');
  const [emailSubject, setEmailSubject] = useState('Critical: Unable to connect to Chicago VPN gateway');
  const [emailBody, setEmailBody] = useState(
    'Hello IT Support,\n\nOur entire Chicago branch office (15 engineers) is currently unable to establish AnyConnect VPN tunnels. The client reports "Certificate handshake timeout". Please investigate immediately as production deployments are stalled.\n\nThank you,\nDavid Miller'
  );
  const [emailResult, setEmailResult] = useState<InboundEmailResponse | null>(null);

  const { data: aiStatus, refetch: refetchAiStatus } = useQuery({
    queryKey: ['ai-status'],
    queryFn: () => aiApi.getStatus(),
  });

  const testAiMutation = useMutation({
    mutationFn: () => aiApi.testConnection(),
    onSuccess: (data) => {
      setTestResult(data);
      refetchAiStatus();
    },
    onError: (err: any) => {
      setTestResult({ status: 'ERROR', message: err.message });
    },
  });

  const emailMutation = useMutation({
    mutationFn: () =>
      emailApi.simulateInboundEmail({
        from: emailFrom,
        senderName: emailName,
        subject: emailSubject,
        body: emailBody,
      }),
    onSuccess: (res) => {
      setEmailResult(res);
    },
  });

  const loadExistingReplyTemplate = () => {
    setEmailSubject('Re: [SD-2026-000001] VPN connectivity update');
    setEmailBody('Here are the traceroute logs you requested:\n\n1 <1 ms <1 ms <1 ms 192.168.1.1\n2 12 ms 11 ms 12 ms 10.0.0.1\n3 * * * Request timed out.\n\nHope this helps expedite triage!');
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div>
        <h1 className="text-xl font-bold text-slate-900 tracking-tight flex items-center gap-2">
          <Settings className="w-6 h-6 text-indigo-600" />
          System & Application Settings
        </h1>
        <p className="text-xs text-slate-500">
          AI triage engine, real-time SSE stream, inbound email webhook simulator, and SLA scheduler health.
        </p>
      </div>

      {/* Backend & DB Health */}
      <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-xs space-y-4">
        <div className="flex items-center gap-3 border-b border-slate-100 pb-3">
          <div className="p-2 bg-emerald-50 text-emerald-600 rounded-lg">
            <Server className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-sm font-bold text-slate-800">Backend Infrastructure & Database</h3>
            <p className="text-xs text-slate-400">Spring Boot 3.3.4 · Java 21 LTS · PostgreSQL / Flyway V1-V3</p>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-xs">
          <div className="p-3 bg-slate-50 rounded-lg space-y-1">
            <span className="text-slate-400 block font-semibold text-[10px] uppercase">Service Health</span>
            <div className="flex items-center gap-1.5 text-emerald-600 font-bold">
              <CheckCircle2 className="w-4 h-4" /> Operational (Actuator UP)
            </div>
          </div>
          <div className="p-3 bg-slate-50 rounded-lg space-y-1">
            <span className="text-slate-400 block font-semibold text-[10px] uppercase">Real-Time Engine</span>
            <div className="flex items-center gap-1.5 text-indigo-600 font-bold">
              <Radio className="w-4 h-4 animate-pulse" /> SSE Stream Active
            </div>
          </div>
          <div className="p-3 bg-slate-50 rounded-lg space-y-1">
            <span className="text-slate-400 block font-semibold text-[10px] uppercase">Database Schema</span>
            <span className="font-semibold text-slate-800">Flyway Migrations (V1, V2, V3)</span>
          </div>
        </div>
      </div>

      {/* AI Triage Configuration & Live Test */}
      <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-xs space-y-4">
        <div className="flex items-center justify-between border-b border-slate-100 pb-3">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-sky-50 text-sky-600 rounded-lg">
              <Cpu className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-slate-800">AI Triage Classifier (OpenAI / Google Gemini)</h3>
              <p className="text-xs text-slate-400">Configured via environment variables with deterministic fallback</p>
            </div>
          </div>

          <button
            onClick={() => testAiMutation.mutate()}
            disabled={testAiMutation.isPending}
            className="flex items-center gap-1.5 px-3 py-1.5 bg-sky-600 hover:bg-sky-700 disabled:opacity-50 text-white rounded-lg text-xs font-semibold shadow-xs transition-colors"
          >
            <Zap className="w-3.5 h-3.5" />
            {testAiMutation.isPending ? 'Testing...' : 'Test AI Connection'}
          </button>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 text-xs">
          <div className="p-3 bg-slate-50 rounded-lg">
            <span className="text-slate-400 block text-[10px] uppercase font-semibold">Configured Provider</span>
            <span className="font-semibold text-slate-800 capitalize">{aiStatus?.provider || 'openai'}</span>
          </div>
          <div className="p-3 bg-slate-50 rounded-lg">
            <span className="text-slate-400 block text-[10px] uppercase font-semibold">Target Model</span>
            <span className="font-mono text-slate-800">{aiStatus?.model || 'gpt-4o-mini'}</span>
          </div>
          <div className="p-3 bg-slate-50 rounded-lg">
            <span className="text-slate-400 block text-[10px] uppercase font-semibold">Status</span>
            <span
              className={`font-semibold ${
                aiStatus?.configured ? 'text-emerald-600' : 'text-amber-600'
              }`}
            >
              {aiStatus?.configured ? 'Live API Key Active' : 'Deterministic Fallback Active'}
            </span>
          </div>
        </div>

        {/* Test Result Output */}
        {testResult && (
          <div className="p-3.5 rounded-lg border bg-slate-900 text-slate-200 text-xs font-mono space-y-2">
            <div className="flex items-center justify-between text-slate-400 pb-1 border-b border-slate-800">
              <span>Test Result: {testResult.status}</span>
              {testResult.latencyMs && <span>Latency: {testResult.latencyMs}ms</span>}
            </div>
            <p className="text-slate-300">{testResult.message || testResult.error}</p>
            {testResult.sampleTriage && (
              <pre className="text-[11px] text-emerald-400 overflow-x-auto bg-slate-950 p-2.5 rounded">
                {JSON.stringify(testResult.sampleTriage, null, 2)}
              </pre>
            )}
          </div>
        )}
      </div>

      {/* Inbound Email Webhook Simulator */}
      <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-xs space-y-4">
        <div className="flex items-center justify-between border-b border-slate-100 pb-3">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-indigo-50 text-indigo-600 rounded-lg">
              <Mail className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-slate-800">Inbound Email Webhook Simulator</h3>
              <p className="text-xs text-slate-400">
                Simulates SendGrid, Mailgun, or AWS SES delivering customer emails to SentinelDesk
              </p>
            </div>
          </div>

          <button
            onClick={loadExistingReplyTemplate}
            className="text-xs text-indigo-600 hover:text-indigo-800 font-semibold underline"
          >
            Load Ticket Reply Template
          </button>
        </div>

        <div className="space-y-3 text-xs">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-slate-600 font-semibold mb-1">From Email (Customer)</label>
              <input
                type="email"
                value={emailFrom}
                onChange={(e) => setEmailFrom(e.target.value)}
                className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
            <div>
              <label className="block text-slate-600 font-semibold mb-1">Sender Name</label>
              <input
                type="text"
                value={emailName}
                onChange={(e) => setEmailName(e.target.value)}
                className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
          </div>

          <div>
            <label className="block text-slate-600 font-semibold mb-1">Email Subject Line</label>
            <input
              type="text"
              value={emailSubject}
              onChange={(e) => setEmailSubject(e.target.value)}
              className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
            <p className="text-[10px] text-slate-400 mt-0.5">
              Tip: Include <code className="font-mono text-indigo-600">Re: [SD-2026-000001]</code> to test automatic comment appending to existing tickets!
            </p>
          </div>

          <div>
            <label className="block text-slate-600 font-semibold mb-1">Email Body Content</label>
            <textarea
              rows={4}
              value={emailBody}
              onChange={(e) => setEmailBody(e.target.value)}
              className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>

          <div className="flex items-center justify-between pt-2">
            <span className="text-[11px] text-slate-400">
              Webhook endpoint: <code className="font-mono text-slate-600">POST /api/v1/email/inbound</code>
            </span>

            <button
              onClick={() => emailMutation.mutate()}
              disabled={emailMutation.isPending || !emailFrom || !emailSubject || !emailBody}
              className="flex items-center gap-1.5 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 text-white rounded-lg text-xs font-semibold shadow-xs transition-colors"
            >
              <Send className="w-3.5 h-3.5" />
              {emailMutation.isPending ? 'Processing Inbound Webhook...' : 'Simulate Inbound Email'}
            </button>
          </div>

          {emailResult && (
            <div className="p-3.5 rounded-lg border border-indigo-200 bg-indigo-50/60 text-indigo-900 space-y-1">
              <div className="flex items-center justify-between">
                <span className="font-bold flex items-center gap-1 text-xs">
                  <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                  Processed: {emailResult.action}
                </span>
                <span className="font-mono font-bold text-xs bg-indigo-200/80 px-2 py-0.5 rounded text-indigo-950">
                  {emailResult.ticketNumber}
                </span>
              </div>
              <p className="text-xs text-indigo-800">{emailResult.message}</p>
            </div>
          )}
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
