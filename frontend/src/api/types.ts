export type Role = 'ADMIN' | 'SUPPORT_MANAGER' | 'SUPPORT_AGENT' | 'REQUESTER';

export type TicketCategory =
  | 'SOFTWARE'
  | 'HARDWARE'
  | 'NETWORK'
  | 'ACCESS'
  | 'SECURITY'
  | 'OUTAGE'
  | 'OTHER';

export type TicketPriority = 'P1' | 'P2' | 'P3' | 'P4';

export type TicketStatus =
  | 'NEW'
  | 'TRIAGED'
  | 'OPEN'
  | 'IN_PROGRESS'
  | 'PENDING_REQUESTER'
  | 'RESOLVED'
  | 'CLOSED'
  | 'CANCELLED';

export interface User {
  id: number;
  email: string;
  fullName: string;
  role: Role;
  department?: string;
  isActive: boolean;
  createdAt: string;
}

export interface TeamMember {
  id: number;
  teamId: number;
  user: User;
  isTeamLead: boolean;
}

export interface Team {
  id: number;
  name: string;
  description?: string;
  categoryFocus?: string;
  members: TeamMember[];
  createdAt: string;
}

export interface SlaPolicy {
  id: number;
  name: string;
  priority: TicketPriority;
  firstResponseHours: number;
  resolutionHours: number;
  description?: string;
  isActive: boolean;
  createdAt: string;
}

export interface SlaTracker {
  id: number;
  ticketId: number;
  policy: SlaPolicy;
  firstResponseDeadline: string;
  firstResponseMet: boolean;
  firstResponseBreached: boolean;
  resolutionDeadline: string;
  resolutionMet: boolean;
  resolutionBreached: boolean;
  isEscalated: boolean;
  warningIssued: boolean;
  remainingMinutes: number;
  isOverdue: boolean;
}

export interface AiTriageSuggestion {
  id: number;
  ticketId: number;
  suggestedCategory: TicketCategory;
  suggestedPriority: TicketPriority;
  summary: string;
  suggestedDomain: string;
  suggestedTeam: string;
  diagnosticQuestions: string[];
  suggestedActions: string[];
  priorityExplanation: string;
  confidenceEstimate: number;
  modelProvider: string;
  modelName: string;
  isAccepted: boolean;
  isOverridden: boolean;
  humanOverrideReason?: string;
  overriddenBy?: User;
  createdAt: string;
}

export interface Incident {
  id: number;
  incidentNumber: string;
  title: string;
  description?: string;
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
  status: 'INVESTIGATING' | 'IDENTIFIED' | 'MONITORING' | 'RESOLVED';
  affectedService: string;
  owner?: User;
  rootCause?: string;
  resolutionSummary?: string;
  postIncidentReview?: string;
  startedAt: string;
  resolvedAt?: string;
  createdAt: string;
  linkedTicketIds: number[];
  linkedTicketCount: number;
}

export interface Ticket {
  id: number;
  ticketNumber: string;
  title: string;
  description: string;
  requester: User;
  assignedAgent?: User;
  assignedTeam?: Team;
  category: TicketCategory;
  priority: TicketPriority;
  status: TicketStatus;
  source: string;
  firstResponseAt?: string;
  resolvedAt?: string;
  closedAt?: string;
  slaDeadline?: string;
  resolutionSummary?: string;
  relatedIncident?: Incident;
  slaTracker?: SlaTracker;
  aiSuggestion?: AiTriageSuggestion;
  createdAt: string;
  updatedAt: string;
}

export interface TicketComment {
  id: number;
  ticketId: number;
  author: User;
  content: string;
  isInternal: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface TicketStatusHistory {
  id: number;
  ticketId: number;
  changedBy?: User;
  oldStatus?: TicketStatus;
  newStatus: TicketStatus;
  reason?: string;
  createdAt: string;
}

export interface TicketAssignmentHistory {
  id: number;
  ticketId: number;
  changedBy?: User;
  oldAgent?: User;
  newAgent?: User;
  oldTeam?: Team;
  newTeam?: Team;
  reason?: string;
  createdAt: string;
}

export interface EscalationEvent {
  id: number;
  ticketId: number;
  ticketNumber: string;
  ticketTitle: string;
  ticketPriority: string;
  escalationLevel: 'WARNING_80' | 'FIRST_RESPONSE_BREACH' | 'RESOLUTION_BREACH';
  reason: string;
  status: 'TRIGGERED' | 'ACKNOWLEDGED' | 'RESOLVED';
  escalatedTo?: User;
  createdAt: string;
  resolvedAt?: string;
}

export interface Notification {
  id: number;
  recipientId: number;
  title: string;
  message: string;
  link?: string;
  isRead: boolean;
  createdAt: string;
}

export interface AuditLog {
  id: number;
  user?: User;
  action: string;
  entityType: string;
  entityId?: string;
  details?: string;
  ipAddress?: string;
  createdAt: string;
}

export interface DashboardSummary {
  totalOpenTickets: number;
  unassignedTickets: number;
  criticalAndHighTickets: number;
  ticketsApproachingSla: number;
  slaBreachCount: number;
  ticketsResolvedToday: number;
  avgFirstResponseMinutes: number;
  avgResolutionHours: number;
  slaComplianceRatePercent: number;
  activeIncidentsCount: number;
}

export interface DashboardCharts {
  ticketsByPriority: { name: string; count: number }[];
  ticketsByCategory: { name: string; count: number }[];
  ticketsByStatus: { name: string; count: number }[];
  agentWorkload: { agentId: number; agentName: string; activeTickets: number }[];
  teamWorkload: { teamId: number; teamName: string; activeTickets: number }[];
  volumeOverTime: { date: string; tickets: number }[];
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}
