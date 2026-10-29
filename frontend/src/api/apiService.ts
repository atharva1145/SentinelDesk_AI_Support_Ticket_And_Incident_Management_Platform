import { api } from './client';
import {
  AiCopilotRequest,
  AiCopilotResponse,
  AiStatusResponse,
  AiTriageSuggestion,
  AuditLog,
  CannedResponse,
  CreateArticleRequest,
  CsatRequest,
  DashboardCharts,
  DashboardSummary,
  EscalationEvent,
  InboundEmailRequest,
  InboundEmailResponse,
  Incident,
  KnowledgeArticle,
  Notification,
  PagedResponse,
  Role,
  SlaPolicy,
  SlaReportSummary,
  SlaTracker,
  Team,
  TeamMember,
  Ticket,
  TicketCategory,
  TicketComment,
  TicketPriority,
  TicketStatus,
  User,
} from './types';

// Auth
export interface LoginResponse {
  token: string;
  type: string;
  user: User;
}

export const authApi = {
  login: (data: { email: string; password: string }) =>
    api.post<LoginResponse>('/auth/login', data),
  register: (data: { email: string; password: string; fullName: string; department?: string }) =>
    api.post<User>('/auth/register', data),
  getCurrentUser: () => api.get<User>('/auth/me'),
  getAllUsers: () => api.get<User[]>('/users'),
  getStaffAndAgents: () => api.get<User[]>('/users/agents'),
  updateUserRole: (userId: number, role: Role) =>
    api.patch<User>(`/admin/users/${userId}/role`, { role }),
  toggleUserStatus: (userId: number, active: boolean) =>
    api.patch<User>(`/admin/users/${userId}/status`, { active }),
};

// Tickets
export interface TicketSearchParams {
  query?: string;
  status?: TicketStatus;
  priority?: TicketPriority;
  category?: TicketCategory;
  agentId?: number;
  teamId?: number;
  unassigned?: boolean;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}

export const ticketApi = {
  getTickets: (params: TicketSearchParams = {}) => {
    const query = new URLSearchParams();
    if (params.query) query.set('query', params.query);
    if (params.status) query.set('status', params.status);
    if (params.priority) query.set('priority', params.priority);
    if (params.category) query.set('category', params.category);
    if (params.agentId) query.set('agentId', params.agentId.toString());
    if (params.teamId) query.set('teamId', params.teamId.toString());
    if (params.unassigned !== undefined) query.set('unassigned', params.unassigned.toString());
    query.set('page', (params.page ?? 0).toString());
    query.set('size', (params.size ?? 15).toString());
    if (params.sortBy) query.set('sortBy', params.sortBy);
    if (params.sortDirection) query.set('sortDirection', params.sortDirection);

    return api.get<PagedResponse<Ticket>>(`/tickets?${query.toString()}`);
  },
  getTicket: (id: number) => api.get<Ticket>(`/tickets/${id}`),
  createTicket: (data: {
    title: string;
    description: string;
    category: TicketCategory;
    priority: TicketPriority;
    assignedTeamId?: number;
  }) => api.post<Ticket>('/tickets', data),
  updateTicket: (id: number, data: { title?: string; description?: string; category?: TicketCategory }) =>
    api.put<Ticket>(`/tickets/${id}`, data),
  changeStatus: (id: number, data: { status: TicketStatus; reason?: string; resolutionSummary?: string }) =>
    api.patch<Ticket>(`/tickets/${id}/status`, data),
  assignTicket: (id: number, data: { agentId?: number | null; teamId?: number; reason?: string }) =>
    api.patch<Ticket>(`/tickets/${id}/assign`, data),
  changePriority: (id: number, data: { priority: TicketPriority; reason: string }) =>
    api.patch<Ticket>(`/tickets/${id}/priority`, data),
  getStatusHistory: (id: number) => api.get<any[]>(`/tickets/${id}/status-history`),
  getAssignmentHistory: (id: number) => api.get<any[]>(`/tickets/${id}/assignment-history`),
  submitCsat: (id: number, data: CsatRequest) =>
    api.post<Ticket>(`/tickets/${id}/csat`, data),
};

// Comments & Notes
export const commentApi = {
  getComments: (ticketId: number) =>
    api.get<TicketComment[]>(`/tickets/${ticketId}/comments`),
  addComment: (ticketId: number, data: { content: string; isInternal?: boolean }) =>
    api.post<TicketComment>(`/tickets/${ticketId}/comments`, data),
};

// Canned Responses
export const cannedResponseApi = {
  getAll: (category?: string) => {
    const params = category ? `?category=${category}` : '';
    return api.get<CannedResponse[]>(`/canned-responses${params}`);
  },
};

// AI Triage
export const aiApi = {
  getSuggestions: (ticketId: number) =>
    api.get<AiTriageSuggestion[]>(`/tickets/${ticketId}/ai-triage`),
  getLatestSuggestion: (ticketId: number) =>
    api.get<AiTriageSuggestion>(`/tickets/${ticketId}/ai-triage/latest`),
  acceptSuggestion: (ticketId: number, suggestionId: number) =>
    api.post<AiTriageSuggestion>(`/tickets/${ticketId}/ai-triage/${suggestionId}/accept`),
  overrideSuggestion: (
    ticketId: number,
    suggestionId: number,
    data: { newCategory: TicketCategory; newPriority: TicketPriority; newTeamId?: number; reason: string }
  ) =>
    api.post<AiTriageSuggestion>(`/tickets/${ticketId}/ai-triage/${suggestionId}/override`, data),
  runCopilot: (ticketId: number, data: AiCopilotRequest) =>
    api.post<AiCopilotResponse>(`/tickets/${ticketId}/ai-triage/copilot`, data),
  getStatus: () => api.get<AiStatusResponse>('/ai/status'),
  testConnection: () => api.post<Record<string, any>>('/ai/test-connection'),
};

// SLA
export const slaApi = {
  getPolicies: () => api.get<SlaPolicy[]>('/sla/policies'),
  updatePolicy: (id: number, data: Partial<SlaPolicy>) =>
    api.put<SlaPolicy>(`/sla/policies/${id}`, data),
  getTracker: (ticketId: number) =>
    api.get<SlaTracker>(`/sla/trackers/${ticketId}`),
};

// Escalations
export const escalationApi = {
  getEscalations: () => api.get<EscalationEvent[]>('/escalations'),
  updateStatus: (id: number, status: 'ACKNOWLEDGED' | 'RESOLVED') =>
    api.patch<EscalationEvent>(`/escalations/${id}/status`, { status }),
};

// Incidents
export const incidentApi = {
  getIncidents: () => api.get<Incident[]>('/incidents'),
  getIncident: (id: number) => api.get<Incident>(`/incidents/${id}`),
  createIncident: (data: {
    title: string;
    description?: string;
    severity: string;
    affectedService: string;
    ownerId?: number;
    ticketIds?: number[];
  }) => api.post<Incident>('/incidents', data),
  linkTickets: (id: number, ticketIds: number[]) =>
    api.post<void>(`/incidents/${id}/link-tickets`, { ticketIds }),
  unlinkTicket: (id: number, ticketId: number) =>
    api.delete<void>(`/incidents/${id}/link-tickets/${ticketId}`),
  updateStatus: (id: number, data: { status: string; rootCause?: string; resolutionSummary?: string }) =>
    api.patch<Incident>(`/incidents/${id}/status`, data),
};

// Teams
export const teamApi = {
  getTeams: () => api.get<Team[]>('/teams'),
  getTeam: (id: number) => api.get<Team>(`/teams/${id}`),
  createTeam: (data: { name: string; description?: string; categoryFocus?: string }) =>
    api.post<Team>('/teams', data),
  addMember: (teamId: number, data: { userId: number; isTeamLead?: boolean }) =>
    api.post<TeamMember>(`/teams/${teamId}/members`, data),
  removeMember: (teamId: number, userId: number) =>
    api.delete<void>(`/teams/${teamId}/members/${userId}`),
};

// Dashboard
export const dashboardApi = {
  getSummary: () => api.get<DashboardSummary>('/dashboard/summary'),
  getCharts: () => api.get<DashboardCharts>('/dashboard/charts'),
};

// Notifications
export const notificationApi = {
  getNotifications: () => api.get<Notification[]>('/notifications'),
  getUnreadCount: () => api.get<{ unreadCount: number }>('/notifications/unread-count'),
  markAsRead: (id: number) => api.patch<void>(`/notifications/${id}/read`),
  markAllRead: () => api.patch<void>('/notifications/mark-all-read'),
};

// Audit Logs
export const auditApi = {
  getLogs: (page = 0, size = 20) =>
    api.get<PagedResponse<AuditLog>>(`/audit-logs?page=${page}&size=${size}`),
};

// Knowledge Base & Runbooks
export const knowledgeBaseApi = {
  searchArticles: (query?: string, category?: string, page = 0, size = 10) => {
    const params = new URLSearchParams({ page: page.toString(), size: size.toString() });
    if (query) params.append('query', query);
    if (category) params.append('category', category);
    return api.get<PagedResponse<KnowledgeArticle>>(`/knowledge-base?${params.toString()}`);
  },
  getArticle: (id: number) => api.get<KnowledgeArticle>(`/knowledge-base/${id}`),
  suggestArticles: (query?: string, category?: string) => {
    const params = new URLSearchParams();
    if (query) params.append('query', query);
    if (category) params.append('category', category);
    return api.get<KnowledgeArticle[]>(`/knowledge-base/suggest?${params.toString()}`);
  },
  createArticle: (data: CreateArticleRequest) => api.post<KnowledgeArticle>('/knowledge-base', data),
  voteHelpful: (id: number) => api.post<KnowledgeArticle>(`/knowledge-base/${id}/vote`),
};

// Inbound Email Ingestion
export const emailApi = {
  simulateInboundEmail: (data: InboundEmailRequest) =>
    api.post<InboundEmailResponse>('/email/inbound', data),
};

// Reports & SLA Compliance Export
export const reportApi = {
  getSlaSummary: () => api.get<SlaReportSummary>('/reports/sla/summary'),
  exportSlaReportCsv: () => {
    const token = localStorage.getItem('token');
    const url = '/api/v1/reports/sla/export';
    fetch(url, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    })
      .then((res) => res.blob())
      .then((blob) => {
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = downloadUrl;
        a.download = `sentineldesk_sla_compliance_${new Date().toISOString().slice(0, 10)}.csv`;
        document.body.appendChild(a);
        a.click();
        a.remove();
      });
  },
};

