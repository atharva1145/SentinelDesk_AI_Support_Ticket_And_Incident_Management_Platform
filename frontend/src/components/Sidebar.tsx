import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard,
  Ticket,
  PlusCircle,
  Inbox,
  Sparkles,
  Clock,
  AlertTriangle,
  Flame,
  Users,
  BarChart3,
  ShieldCheck,
  UserCog,
  Settings,
  Headphones,
} from 'lucide-react';

export const Sidebar: React.FC = () => {
  const { user, isStaff, isAdmin } = useAuth();

  const navigation = [
    { name: 'Dashboard', href: '/', icon: LayoutDashboard, roles: ['ALL'] },
    { name: 'My Tickets', href: '/my-tickets', icon: Ticket, roles: ['ALL'] },
    { name: 'All Tickets', href: '/tickets', icon: Headphones, roles: ['STAFF'] },
    { name: 'Create Ticket', href: '/tickets/new', icon: PlusCircle, roles: ['ALL'] },
    { name: 'Assignment Queue', href: '/assignment-queue', icon: Inbox, roles: ['STAFF'] },
    { name: 'AI Triage Review', href: '/ai-triage', icon: Sparkles, roles: ['STAFF'] },
    { name: 'SLA Monitoring', href: '/sla', icon: Clock, roles: ['ALL'] },
    { name: 'Escalations', href: '/escalations', icon: AlertTriangle, roles: ['STAFF'] },
    { name: 'Incidents', href: '/incidents', icon: Flame, roles: ['ALL'] },
    { name: 'Teams & Agents', href: '/teams', icon: Users, roles: ['ALL'] },
    { name: 'Reports & Analytics', href: '/reports', icon: BarChart3, roles: ['STAFF'] },
    { name: 'Audit Logs', href: '/audit-logs', icon: ShieldCheck, roles: ['ADMIN'] },
    { name: 'User Management', href: '/admin/users', icon: UserCog, roles: ['ADMIN'] },
    { name: 'Settings', href: '/settings', icon: Settings, roles: ['ALL'] },
  ];

  const filteredNav = navigation.filter((item) => {
    if (item.roles.includes('ALL')) return true;
    if (item.roles.includes('STAFF') && isStaff) return true;
    if (item.roles.includes('ADMIN') && isAdmin) return true;
    return false;
  });

  return (
    <aside className="w-64 bg-slate-900 text-slate-300 flex flex-col shrink-0 min-h-screen border-r border-slate-800">
      {/* Brand Header */}
      <div className="h-16 flex items-center px-6 gap-3 border-b border-slate-800 bg-slate-950/40">
        <div className="w-9 h-9 rounded-lg bg-sky-600 flex items-center justify-center text-white shadow-lg shadow-sky-600/30">
          <ShieldCheck className="w-5 h-5" />
        </div>
        <div>
          <span className="font-bold text-white text-base tracking-tight block">SentinelDesk</span>
          <span className="text-[10px] text-sky-400 font-semibold tracking-wider uppercase block">AI Incident Platform</span>
        </div>
      </div>

      {/* Nav List */}
      <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
        <div className="text-[11px] font-semibold text-slate-500 uppercase tracking-wider px-3 mb-2">
          Operations & ITSM
        </div>
        {filteredNav.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.name}
              to={item.href}
              end={item.href === '/'}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2 text-sm font-medium rounded-lg transition-colors ${
                  isActive
                    ? 'bg-sky-600 text-white shadow-sm'
                    : 'text-slate-400 hover:text-white hover:bg-slate-800/60'
                }`
              }
            >
              <Icon className="w-4 h-4 shrink-0" />
              <span>{item.name}</span>
            </NavLink>
          );
        })}
      </nav>

      {/* User info at bottom */}
      <div className="p-4 border-t border-slate-800 bg-slate-950/30">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-full bg-slate-700 flex items-center justify-center text-xs font-bold text-white">
            {user?.fullName?.charAt(0) || 'U'}
          </div>
          <div className="min-w-0 flex-1">
            <p className="text-xs font-medium text-white truncate">{user?.fullName}</p>
            <p className="text-[11px] text-slate-400 truncate capitalize">{user?.role?.toLowerCase().replace('_', ' ')}</p>
          </div>
        </div>
      </div>
    </aside>
  );
};
