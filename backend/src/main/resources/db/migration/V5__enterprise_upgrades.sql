-- Flyway Migration V5: Attachments, Business Hours SLA, and Routing Optimization

-- 1. Create ticket and incident attachments table
CREATE TABLE IF NOT EXISTS attachments (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT REFERENCES tickets(id) ON DELETE CASCADE,
    incident_id BIGINT REFERENCES incidents(id) ON DELETE CASCADE,
    filename VARCHAR(255) NOT NULL,
    stored_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    uploaded_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_attachments_ticket_id ON attachments(ticket_id);
CREATE INDEX IF NOT EXISTS idx_attachments_incident_id ON attachments(incident_id);

-- 2. Add business hours flag to SLA policies
ALTER TABLE sla_policies ADD COLUMN IF NOT EXISTS is_business_hours BOOLEAN NOT NULL DEFAULT TRUE;

-- Update P1 policies to 24x7 (continuous calendar time) and P2-P4 to business hours
UPDATE sla_policies SET is_business_hours = FALSE WHERE priority = 'P1';
UPDATE sla_policies SET is_business_hours = TRUE WHERE priority IN ('P2', 'P3', 'P4');

-- 3. Optimization index for agent workload calculations
CREATE INDEX IF NOT EXISTS idx_tickets_agent_status ON tickets(assigned_agent_id, status);
