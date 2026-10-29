import React, { useEffect, useState } from 'react';
import { teamApi, authApi } from '../api/apiService';
import { Team, User } from '../api/types';
import { Modal } from '../components/Modal';
import { useAuth } from '../context/AuthContext';
import { Users, Plus, ShieldCheck, UserPlus, Trash2, Mail } from 'lucide-react';

export const TeamsAndAgents: React.FC = () => {
  const { isManager } = useAuth();
  const [teams, setTeams] = useState<Team[]>([]);
  const [staff, setStaff] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);

  // Create Team Modal
  const [showCreateTeamModal, setShowCreateTeamModal] = useState(false);
  const [teamName, setTeamName] = useState('');
  const [teamDesc, setTeamDesc] = useState('');
  const [categoryFocus, setCategoryFocus] = useState('SOFTWARE');

  // Add Member Modal
  const [activeTeam, setActiveTeam] = useState<Team | null>(null);
  const [selectedUserId, setSelectedUserId] = useState('');
  const [isLead, setIsLead] = useState(false);

  const loadData = async () => {
    setLoading(true);
    try {
      const [teamsRes, staffRes] = await Promise.all([
        teamApi.getTeams(),
        authApi.getStaffAndAgents(),
      ]);
      setTeams(teamsRes);
      setStaff(staffRes);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleCreateTeam = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await teamApi.createTeam({
        name: teamName,
        description: teamDesc,
        categoryFocus,
      });
      setShowCreateTeamModal(false);
      setTeamName('');
      setTeamDesc('');
      loadData();
    } catch (err: any) {
      alert(err.message || 'Failed to create team');
    }
  };

  const handleAddMember = async () => {
    if (!activeTeam || !selectedUserId) return;
    try {
      await teamApi.addMember(activeTeam.id, {
        userId: Number(selectedUserId),
        isTeamLead: isLead,
      });
      setActiveTeam(null);
      setSelectedUserId('');
      setIsLead(false);
      loadData();
    } catch (err: any) {
      alert(err.message || 'Failed to add member');
    }
  };

  const handleRemoveMember = async (teamId: number, userId: number) => {
    if (!confirm('Are you sure you want to remove this member from the team?')) return;
    try {
      await teamApi.removeMember(teamId, userId);
      loadData();
    } catch (err: any) {
      alert(err.message || 'Failed to remove member');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">Teams & Support Agents</h1>
          <p className="text-xs text-slate-500">
            Support organization structure, specialized category routing, and active agent roster
          </p>
        </div>
        {isManager && (
          <button
            onClick={() => setShowCreateTeamModal(true)}
            className="inline-flex items-center gap-1.5 px-4 py-2 bg-sky-600 hover:bg-sky-700 text-white text-xs font-semibold rounded-lg shadow-sm transition-colors"
          >
            <Plus className="w-4 h-4" />
            <span>Create New Team</span>
          </button>
        )}
      </div>

      {loading ? (
        <div className="p-12 text-center bg-white rounded-xl border border-slate-200">
          <div className="w-6 h-6 border-3 border-sky-600 border-t-transparent rounded-full animate-spin mx-auto" />
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {teams.map((tm) => (
            <div
              key={tm.id}
              className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden flex flex-col justify-between"
            >
              <div className="p-5 border-b border-slate-100 flex items-start justify-between">
                <div>
                  <div className="flex items-center gap-2">
                    <h3 className="text-base font-bold text-slate-800">{tm.name}</h3>
                    {tm.categoryFocus && (
                      <span className="px-2 py-0.5 rounded text-[10px] font-semibold bg-sky-50 text-sky-700 border border-sky-100">
                        {tm.categoryFocus}
                      </span>
                    )}
                  </div>
                  <p className="text-xs text-slate-500 mt-1">{tm.description || 'General support team'}</p>
                </div>
                {isManager && (
                  <button
                    onClick={() => setActiveTeam(tm)}
                    className="p-1.5 text-sky-600 hover:bg-sky-50 rounded-lg border border-sky-200 text-xs font-semibold flex items-center gap-1 transition-colors"
                  >
                    <UserPlus className="w-3.5 h-3.5" />
                    <span>Add Agent</span>
                  </button>
                )}
              </div>

              {/* Members List */}
              <div className="p-5 flex-1">
                <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400 block mb-2">
                  Assigned Team Members ({tm.members.length})
                </span>
                {tm.members.length === 0 ? (
                  <p className="text-xs text-slate-400 italic py-2">No agents assigned to this team yet.</p>
                ) : (
                  <div className="divide-y divide-slate-100">
                    {tm.members.map((mem) => (
                      <div key={mem.id} className="py-2.5 flex items-center justify-between text-xs">
                        <div className="flex items-center gap-2.5">
                          <div className="w-7 h-7 rounded-full bg-slate-100 text-slate-700 font-bold flex items-center justify-center text-xs shrink-0">
                            {mem.user.fullName.charAt(0)}
                          </div>
                          <div>
                            <div className="flex items-center gap-1.5">
                              <span className="font-semibold text-slate-800">{mem.user.fullName}</span>
                              {mem.isTeamLead && (
                                <span className="px-1.5 py-0.2 bg-amber-50 text-amber-800 border border-amber-200 rounded text-[9px] font-bold">
                                  Lead
                                </span>
                              )}
                            </div>
                            <span className="text-[10px] text-slate-400">{mem.user.email}</span>
                          </div>
                        </div>

                        {isManager && (
                          <button
                            onClick={() => handleRemoveMember(tm.id, mem.user.id)}
                            className="p-1 text-slate-400 hover:text-rose-600 rounded transition-colors"
                            title="Remove from team"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Create Team Modal */}
      <Modal isOpen={showCreateTeamModal} onClose={() => setShowCreateTeamModal(false)} title="Create Support Team">
        <form onSubmit={handleCreateTeam} className="space-y-4 text-xs">
          <div>
            <label className="block font-semibold uppercase text-slate-600 mb-1">Team Name *</label>
            <input
              type="text"
              required
              value={teamName}
              onChange={(e) => setTeamName(e.target.value)}
              placeholder="e.g. Database Reliability Engineering (DBRE)"
              className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg text-xs"
            />
          </div>

          <div>
            <label className="block font-semibold uppercase text-slate-600 mb-1">Category Focus</label>
            <select
              value={categoryFocus}
              onChange={(e) => setCategoryFocus(e.target.value)}
              className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg text-xs"
            >
              <option value="OUTAGE">Production Outages</option>
              <option value="NETWORK">Network Engineering</option>
              <option value="SECURITY">Security Incident Response</option>
              <option value="ACCESS">Access & Identity</option>
              <option value="SOFTWARE">Application Software</option>
              <option value="HARDWARE">Hardware Support</option>
              <option value="OTHER">General Operations</option>
            </select>
          </div>

          <div>
            <label className="block font-semibold uppercase text-slate-600 mb-1">Mission / Description</label>
            <textarea
              rows={3}
              value={teamDesc}
              onChange={(e) => setTeamDesc(e.target.value)}
              placeholder="Outline the responsibilities and scope of this support team..."
              className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg text-xs"
            />
          </div>

          <div className="flex justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={() => setShowCreateTeamModal(false)}
              className="px-3 py-1.5 text-slate-600 hover:text-slate-800"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-1.5 bg-sky-600 hover:bg-sky-700 text-white font-semibold rounded-lg"
            >
              Create Team
            </button>
          </div>
        </form>
      </Modal>

      {/* Add Member Modal */}
      <Modal
        isOpen={!!activeTeam}
        onClose={() => setActiveTeam(null)}
        title={`Add Member to ${activeTeam?.name || ''}`}
      >
        <div className="space-y-4 text-xs">
          <div>
            <label className="block font-semibold uppercase text-slate-600 mb-1">Select Support Agent *</label>
            <select
              value={selectedUserId}
              onChange={(e) => setSelectedUserId(e.target.value)}
              className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg text-xs"
            >
              <option value="">Choose an agent...</option>
              {staff.map((st) => (
                <option key={st.id} value={st.id}>
                  {st.fullName} ({st.email})
                </option>
              ))}
            </select>
          </div>

          <label className="flex items-center gap-2 cursor-pointer font-medium text-slate-700">
            <input
              type="checkbox"
              checked={isLead}
              onChange={(e) => setIsLead(e.target.checked)}
              className="w-4 h-4 rounded text-sky-600 border-slate-300 focus:ring-sky-500"
            />
            <span>Assign as Team Lead</span>
          </label>

          <div className="flex justify-end gap-2 pt-2">
            <button
              onClick={() => setActiveTeam(null)}
              className="px-3 py-1.5 text-slate-600 hover:text-slate-800"
            >
              Cancel
            </button>
            <button
              onClick={handleAddMember}
              disabled={!selectedUserId}
              className="px-4 py-1.5 bg-sky-600 hover:bg-sky-700 text-white font-semibold rounded-lg disabled:opacity-40"
            >
              Add to Team
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
