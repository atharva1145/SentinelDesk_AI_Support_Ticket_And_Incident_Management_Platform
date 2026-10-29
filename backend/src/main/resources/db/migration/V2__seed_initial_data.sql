-- V2__seed_initial_data.sql
-- Seed default SLA policies, teams, users, and initial test data

-- 1. SLA POLICIES
INSERT INTO sla_policies (name, priority, first_response_hours, resolution_hours, description)
VALUES 
('Critical Priority SLA (P1)', 'P1', 0.25, 4.00, 'Urgent critical outage affecting core business operations. Response within 15 mins, resolution within 4 hours.'),
('High Priority SLA (P2)', 'P2', 1.00, 8.00, 'Major degradation or high impact issue without immediate workaround. Response within 1 hr, resolution within 8 hours.'),
('Medium Priority SLA (P3)', 'P3', 4.00, 48.00, 'Standard issue or localized disruption with existing workaround. Response within 4 hrs, resolution within 2 days.'),
('Low Priority SLA (P4)', 'P4', 24.00, 120.00, 'Routine service requests, questions, or cosmetic bugs. Response within 24 hrs, resolution within 5 days.');

-- 2. TEAMS
INSERT INTO teams (name, description, category_focus)
VALUES
('Tier 1 Service Desk', 'Frontline technical triage and general user support.', 'SOFTWARE'),
('Network Operations (NOC)', 'Core network, switches, firewalls, and connectivity.', 'NETWORK'),
('Cloud & Infrastructure', 'Servers, databases, Kubernetes, and virtualization.', 'OUTAGE'),
('Security Operations (SOC)', 'Identity access, security alerts, and threat incidents.', 'SECURITY');

-- 3. USERS (Default password will be ensured by DataInitializer: Password123!)
INSERT INTO users (email, password_hash, full_name, role, department, is_active)
VALUES
('admin@sentineldesk.com', '$2a$10$7Z8V4W7VwX7aCjHhZf5Z7eI3m1fK4s5hD8a0B9c8d7e6f5a4b3c2', 'System Administrator', 'ADMIN', 'IT Operations', TRUE),
('manager@sentineldesk.com', '$2a$10$7Z8V4W7VwX7aCjHhZf5Z7eI3m1fK4s5hD8a0B9c8d7e6f5a4b3c2', 'Sarah Connor', 'SUPPORT_MANAGER', 'IT Service Management', TRUE),
('agent1@sentineldesk.com', '$2a$10$7Z8V4W7VwX7aCjHhZf5Z7eI3m1fK4s5hD8a0B9c8d7e6f5a4b3c2', 'Alex Rivera', 'SUPPORT_AGENT', 'Tier 1 Service Desk', TRUE),
('agent2@sentineldesk.com', '$2a$10$7Z8V4W7VwX7aCjHhZf5Z7eI3m1fK4s5hD8a0B9c8d7e6f5a4b3c2', 'Elena Rostova', 'SUPPORT_AGENT', 'Network Operations', TRUE),
('user@sentineldesk.com', '$2a$10$7Z8V4W7VwX7aCjHhZf5Z7eI3m1fK4s5hD8a0B9c8d7e6f5a4b3c2', 'David Miller', 'REQUESTER', 'Sales & Marketing', TRUE),
('user2@sentineldesk.com', '$2a$10$7Z8V4W7VwX7aCjHhZf5Z7eI3m1fK4s5hD8a0B9c8d7e6f5a4b3c2', 'Rachel Green', 'REQUESTER', 'Finance', TRUE);

-- 4. TEAM MEMBERSHIPS
-- Alex in Tier 1 Service Desk
INSERT INTO team_memberships (team_id, user_id, is_team_lead)
SELECT t.id, u.id, TRUE FROM teams t, users u WHERE t.name = 'Tier 1 Service Desk' AND u.email = 'agent1@sentineldesk.com';

-- Elena in Network Operations
INSERT INTO team_memberships (team_id, user_id, is_team_lead)
SELECT t.id, u.id, TRUE FROM teams t, users u WHERE t.name = 'Network Operations (NOC)' AND u.email = 'agent2@sentineldesk.com';
