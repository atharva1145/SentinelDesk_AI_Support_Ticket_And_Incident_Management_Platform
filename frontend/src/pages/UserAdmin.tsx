import React, { useEffect, useState } from 'react';
import { authApi } from '../api/apiService';
import { User, Role } from '../api/types';
import { UserCog, ShieldCheck, Check, Ban } from 'lucide-react';

export const UserAdmin: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);

  const loadUsers = async () => {
    setLoading(true);
    try {
      const res = await authApi.getAllUsers();
      setUsers(res);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUsers();
  }, []);

  const handleRoleChange = async (userId: number, newRole: Role) => {
    try {
      await authApi.updateUserRole(userId, newRole);
      loadUsers();
    } catch (err: any) {
      alert(err.message || 'Failed to update user role');
    }
  };

  const handleToggleStatus = async (userId: number, currentActive: boolean) => {
    try {
      await authApi.toggleUserStatus(userId, !currentActive);
      loadUsers();
    } catch (err: any) {
      alert(err.message || 'Failed to update user status');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">User Administration & RBAC</h1>
          <p className="text-xs text-slate-500">
            Manage user accounts, assign service desk roles, and control application access
          </p>
        </div>
        <span className="text-xs text-slate-500 bg-white px-3 py-1.5 rounded-lg border border-slate-200">
          Total Users: <span className="font-bold text-slate-800">{users.length}</span>
        </span>
      </div>

      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
        {loading ? (
          <div className="p-12 text-center">
            <div className="w-6 h-6 border-3 border-sky-600 border-t-transparent rounded-full animate-spin mx-auto" />
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-slate-500 text-xs font-semibold uppercase tracking-wider border-b border-slate-100">
                <tr>
                  <th className="py-3 px-4">User</th>
                  <th className="py-3 px-4">Department</th>
                  <th className="py-3 px-4">Assigned Role</th>
                  <th className="py-3 px-4">Account Status</th>
                  <th className="py-3 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-slate-700">
                {users.map((u) => (
                  <tr key={u.id} className="hover:bg-slate-50 transition-colors">
                    <td className="py-3.5 px-4 whitespace-nowrap">
                      <div className="flex items-center gap-2.5">
                        <div className="w-8 h-8 rounded-full bg-slate-100 text-slate-700 font-bold flex items-center justify-center text-xs">
                          {u.fullName.charAt(0)}
                        </div>
                        <div>
                          <span className="font-semibold text-slate-800 block text-xs">{u.fullName}</span>
                          <span className="text-[11px] text-slate-400">{u.email}</span>
                        </div>
                      </div>
                    </td>
                    <td className="py-3.5 px-4 whitespace-nowrap text-xs text-slate-600">
                      {u.department || '—'}
                    </td>
                    <td className="py-3.5 px-4 whitespace-nowrap">
                      <select
                        value={u.role}
                        onChange={(e) => handleRoleChange(u.id, e.target.value as Role)}
                        className="text-xs bg-slate-50 border border-slate-200 rounded-md px-2 py-1 text-slate-700 font-medium focus:outline-hidden"
                      >
                        <option value="ADMIN">ADMIN</option>
                        <option value="SUPPORT_MANAGER">SUPPORT_MANAGER</option>
                        <option value="SUPPORT_AGENT">SUPPORT_AGENT</option>
                        <option value="REQUESTER">REQUESTER</option>
                      </select>
                    </td>
                    <td className="py-3.5 px-4 whitespace-nowrap">
                      {u.isActive ? (
                        <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
                          Active
                        </span>
                      ) : (
                        <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-rose-50 text-rose-700 border border-rose-200">
                          Disabled
                        </span>
                      )}
                    </td>
                    <td className="py-3.5 px-4 text-right whitespace-nowrap">
                      <button
                        onClick={() => handleToggleStatus(u.id, u.isActive)}
                        className={`px-2.5 py-1 text-xs font-semibold rounded-md border transition-colors ${
                          u.isActive
                            ? 'text-rose-700 bg-rose-50 hover:bg-rose-100 border-rose-200'
                            : 'text-emerald-700 bg-emerald-50 hover:bg-emerald-100 border-emerald-200'
                        }`}
                      >
                        {u.isActive ? 'Deactivate' : 'Activate'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
