import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { authApi } from '../api/apiService';
import { ShieldCheck, ArrowRight, UserCheck, AlertCircle } from 'lucide-react';

export const Login: React.FC = () => {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [isRegister, setIsRegister] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [department, setDepartment] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      if (isRegister) {
        await authApi.register({ email, password, fullName, department });
        await login(email, password);
      } else {
        await login(email, password);
      }
      navigate('/');
    } catch (err: any) {
      setError(err.message || 'Authentication failed');
    } finally {
      setLoading(false);
    }
  };

  const handleQuickLogin = async (demoEmail: string) => {
    setError(null);
    setLoading(true);
    try {
      await login(demoEmail, 'Password123!');
      navigate('/');
    } catch (err: any) {
      setError(err.message || 'Demo login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center">
        <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-sky-600 text-white shadow-xl shadow-sky-600/30 mb-4">
          <ShieldCheck className="w-8 h-8" />
        </div>
        <h2 className="text-3xl font-extrabold text-white tracking-tight">SentinelDesk AI</h2>
        <p className="mt-2 text-sm text-slate-400">
          Support Ticket & Incident Management Platform with SLA Engine
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-slate-900 py-8 px-6 shadow-2xl rounded-2xl border border-slate-800 sm:px-10">
          {error && (
            <div className="mb-4 p-3 bg-rose-500/10 border border-rose-500/30 rounded-lg flex items-center gap-2 text-xs text-rose-400">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <form className="space-y-4" onSubmit={handleSubmit}>
            {isRegister && (
              <>
                <div>
                  <label className="block text-xs font-medium text-slate-300">Full Name</label>
                  <input
                    type="text"
                    required
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    className="mt-1 block w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-white text-sm placeholder-slate-500 focus:outline-hidden focus:ring-2 focus:ring-sky-500"
                    placeholder="David Miller"
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-slate-300">Department</label>
                  <input
                    type="text"
                    value={department}
                    onChange={(e) => setDepartment(e.target.value)}
                    className="mt-1 block w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-white text-sm placeholder-slate-500 focus:outline-hidden focus:ring-2 focus:ring-sky-500"
                    placeholder="Sales & Marketing"
                  />
                </div>
              </>
            )}

            <div>
              <label className="block text-xs font-medium text-slate-300">Work Email</label>
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="mt-1 block w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-white text-sm placeholder-slate-500 focus:outline-hidden focus:ring-2 focus:ring-sky-500"
                placeholder="user@sentineldesk.com"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-300">Password</label>
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="mt-1 block w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-white text-sm placeholder-slate-500 focus:outline-hidden focus:ring-2 focus:ring-sky-500"
                placeholder="••••••••••••"
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full mt-2 flex items-center justify-center gap-2 py-2.5 px-4 rounded-lg text-sm font-semibold text-white bg-sky-600 hover:bg-sky-500 focus:outline-hidden focus:ring-2 focus:ring-offset-2 focus:ring-sky-500 shadow-md transition-all disabled:opacity-50"
            >
              <span>{isRegister ? 'Create Requester Account' : 'Sign In'}</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </form>

          <div className="mt-4 text-center">
            <button
              type="button"
              onClick={() => setIsRegister(!isRegister)}
              className="text-xs text-sky-400 hover:text-sky-300 transition-colors"
            >
              {isRegister ? 'Already have an account? Sign In' : 'Need an account? Register as Requester'}
            </button>
          </div>

          {/* Quick Demo Logins */}
          <div className="mt-6 pt-6 border-t border-slate-800">
            <p className="text-xs font-medium text-slate-400 mb-2 flex items-center gap-1.5">
              <UserCheck className="w-3.5 h-3.5 text-sky-400" />
              <span>One-Click Role Demonstration</span>
            </p>
            <div className="grid grid-cols-2 gap-2 text-xs">
              <button
                type="button"
                onClick={() => handleQuickLogin('admin@sentineldesk.com')}
                className="p-2 bg-slate-800/80 hover:bg-slate-700 text-slate-200 rounded-lg border border-slate-700 text-left transition-colors"
              >
                <span className="font-semibold text-white block">Admin</span>
                <span className="text-[10px] text-slate-400 block truncate">admin@sentineldesk.com</span>
              </button>
              <button
                type="button"
                onClick={() => handleQuickLogin('manager@sentineldesk.com')}
                className="p-2 bg-slate-800/80 hover:bg-slate-700 text-slate-200 rounded-lg border border-slate-700 text-left transition-colors"
              >
                <span className="font-semibold text-white block">Support Manager</span>
                <span className="text-[10px] text-slate-400 block truncate">manager@sentineldesk.com</span>
              </button>
              <button
                type="button"
                onClick={() => handleQuickLogin('agent1@sentineldesk.com')}
                className="p-2 bg-slate-800/80 hover:bg-slate-700 text-slate-200 rounded-lg border border-slate-700 text-left transition-colors"
              >
                <span className="font-semibold text-white block">Senior Agent</span>
                <span className="text-[10px] text-slate-400 block truncate">agent1@sentineldesk.com</span>
              </button>
              <button
                type="button"
                onClick={() => handleQuickLogin('user@sentineldesk.com')}
                className="p-2 bg-slate-800/80 hover:bg-slate-700 text-slate-200 rounded-lg border border-slate-700 text-left transition-colors"
              >
                <span className="font-semibold text-white block">Requester</span>
                <span className="text-[10px] text-slate-400 block truncate">user@sentineldesk.com</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
