import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Layout } from './components/Layout';

// Pages
import { Login } from './pages/Login';
import { Dashboard } from './pages/Dashboard';
import { MyTickets } from './pages/MyTickets';
import { AllTickets } from './pages/AllTickets';
import { CreateTicket } from './pages/CreateTicket';
import { TicketDetails } from './pages/TicketDetails';
import { AssignmentQueue } from './pages/AssignmentQueue';
import { AiTriageReview } from './pages/AiTriageReview';
import { SlaMonitoring } from './pages/SlaMonitoring';
import { Escalations } from './pages/Escalations';
import { Incidents } from './pages/Incidents';
import { TeamsAndAgents } from './pages/TeamsAndAgents';
import { ReportsAnalytics } from './pages/ReportsAnalytics';
import { Notifications } from './pages/Notifications';
import { AuditLogs } from './pages/AuditLogs';
import { UserAdmin } from './pages/UserAdmin';
import { ApplicationSettings } from './pages/ApplicationSettings';
import { KnowledgeBase } from './pages/KnowledgeBase';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
    },
  },
});

const ProtectedRoute: React.FC<{ children: React.ReactNode; staffOnly?: boolean; adminOnly?: boolean }> = ({
  children,
  staffOnly,
  adminOnly,
}) => {
  const { user, isLoading, isStaff, isAdmin } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50">
        <div className="w-8 h-8 border-4 border-sky-600 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (adminOnly && !isAdmin) {
    return <Navigate to="/" replace />;
  }

  if (staffOnly && !isStaff) {
    return <Navigate to="/my-tickets" replace />;
  }

  return <>{children}</>;
};

export const App: React.FC = () => {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<Login />} />

            <Route
              path="/"
              element={
                <ProtectedRoute>
                  <Layout />
                </ProtectedRoute>
              }
            >
              <Route index element={<Dashboard />} />
              <Route path="my-tickets" element={<MyTickets />} />
              <Route
                path="tickets"
                element={
                  <ProtectedRoute staffOnly>
                    <AllTickets />
                  </ProtectedRoute>
                }
              />
              <Route path="tickets/new" element={<CreateTicket />} />
              <Route path="knowledge-base" element={<KnowledgeBase />} />
              <Route path="tickets/:id" element={<TicketDetails />} />
              <Route
                path="assignment-queue"
                element={
                  <ProtectedRoute staffOnly>
                    <AssignmentQueue />
                  </ProtectedRoute>
                }
              />
              <Route
                path="ai-triage"
                element={
                  <ProtectedRoute staffOnly>
                    <AiTriageReview />
                  </ProtectedRoute>
                }
              />
              <Route path="sla" element={<SlaMonitoring />} />
              <Route
                path="escalations"
                element={
                  <ProtectedRoute staffOnly>
                    <Escalations />
                  </ProtectedRoute>
                }
              />
              <Route path="incidents" element={<Incidents />} />
              <Route path="teams" element={<TeamsAndAgents />} />
              <Route
                path="reports"
                element={
                  <ProtectedRoute staffOnly>
                    <ReportsAnalytics />
                  </ProtectedRoute>
                }
              />
              <Route path="notifications" element={<Notifications />} />
              <Route
                path="audit-logs"
                element={
                  <ProtectedRoute adminOnly>
                    <AuditLogs />
                  </ProtectedRoute>
                }
              />
              <Route
                path="admin/users"
                element={
                  <ProtectedRoute adminOnly>
                    <UserAdmin />
                  </ProtectedRoute>
                }
              />
              <Route path="settings" element={<ApplicationSettings />} />
            </Route>

            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  );
};

export default App;
