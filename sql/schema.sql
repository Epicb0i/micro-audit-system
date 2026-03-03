-- ============================================
-- Micro-Audit System — Database Schema
-- MySQL 8.x
-- ============================================

CREATE DATABASE IF NOT EXISTS micro_audit;
USE micro_audit;

-- -------------------------------------------
-- 1. committees
-- -------------------------------------------
CREATE TABLE committees (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100)   NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMP      DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- -------------------------------------------
-- 2. users
-- -------------------------------------------
CREATE TABLE users (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    staff_id     VARCHAR(50)    NOT NULL UNIQUE,
    name         VARCHAR(100)   NOT NULL,
    email        VARCHAR(100),
    phone        VARCHAR(20),
    password     VARCHAR(255)   NOT NULL,
    role         ENUM('Member','Head','Auditor') NOT NULL DEFAULT 'Member',
    department   VARCHAR(100),
    committee_id INT,
    created_at   TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (committee_id) REFERENCES committees(id)
) ENGINE=InnoDB;

-- -------------------------------------------
-- 3. actions
-- -------------------------------------------
CREATE TABLE actions (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    action_code     VARCHAR(30)    NOT NULL UNIQUE,
    title           VARCHAR(200)   NOT NULL,
    type            VARCHAR(50),
    description     TEXT,
    priority        ENUM('low','medium','high') DEFAULT 'low',
    status          ENUM('Created','Review','Approved','Rejected','Closed') DEFAULT 'Created',
    committee_id    INT            NOT NULL,
    created_by      INT            NOT NULL,
    approver_id     INT,
    estimated_cost  DECIMAL(12,2),
    expected_close  DATETIME       NOT NULL,
    actual_close    DATETIME,
    created_at      TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (committee_id) REFERENCES committees(id),
    FOREIGN KEY (created_by)   REFERENCES users(id),
    FOREIGN KEY (approver_id)  REFERENCES users(id)
) ENGINE=InnoDB;

-- -------------------------------------------
-- 4. audit_logs (micro-audit trail)
-- -------------------------------------------
CREATE TABLE audit_logs (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    action_id   INT,
    user_id     INT,
    activity    VARCHAR(255)   NOT NULL,
    details     TEXT,
    ip_address  VARCHAR(45),
    session_id  VARCHAR(100),
    role        VARCHAR(20),
    created_at  TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (action_id) REFERENCES actions(id),
    FOREIGN KEY (user_id)   REFERENCES users(id)
) ENGINE=InnoDB;

-- -------------------------------------------
-- Seed data — 6 committees
-- -------------------------------------------
INSERT INTO committees (name, description) VALUES
('Exam Committee',     'Handles examination scheduling, hall booking, and result processing'),
('T&P Committee',      'Training & placement activities, campus drives'),
('Cultural Committee', 'Cultural events, festivals, and student engagement'),
('NAAC Committee',     'Accreditation documentation and compliance'),
('Purchase Committee', 'Equipment procurement, vendor management'),
('Hostel Committee',   'Hostel maintenance, mess management, discipline');

-- -------------------------------------------
-- Seed data — sample users (password = "password123")
-- -------------------------------------------
INSERT INTO users (staff_id, name, email, password, role, department, committee_id) VALUES
('STAFF_2024_001', 'Dr. A. Sharma',  'sharma@college.edu',  'password123', 'Head',    'Computer Engineering', 1),
('STAFF_2024_002', 'Prof. R. Mehta',  'mehta@college.edu',   'password123', 'Head',    'Mechanical',           5),
('STAFF_2024_003', 'Dr. P. Joshi',    'joshi@college.edu',   'password123', 'Head',    'Electronics',          3),
('STAFF_2024_004', 'Dr. S. Patil',    'patil@college.edu',   'password123', 'Head',    'Information Technology',4),
('STAFF_2024_005', 'Gaurav Chopde',   'gaurav@college.edu',  'password123', 'Member',  'Computer Engineering', 1),
('STAFF_2024_006', 'Admin Auditor',   'auditor@college.edu', 'password123', 'Auditor', 'Administration',       NULL);

-- -------------------------------------------
-- Seed data — sample actions
-- -------------------------------------------
INSERT INTO actions (action_code, title, type, description, priority, status, committee_id, created_by, approver_id, estimated_cost, expected_close, actual_close) VALUES
('ACT-001', 'Equipment Purchase Approval',  'Purchase Request',     'Procurement of lab equipment for CSE dept.', 'high',   'Review',   5, 5, 2, 150000.00, '2025-02-05 17:00:00', NULL),
('ACT-002', 'Exam Hall Booking',            'Event Approval',       'Book hall A1-A5 for mid-term exams.',        'medium', 'Approved', 1, 5, 1, NULL,      '2025-02-02 17:00:00', '2025-02-01 14:30:00'),
('ACT-003', 'Cultural Fest Budget Sanction', 'Budget Sanction',     'Annual techno-cultural fest budget approval.','high',   'Created',  3, 5, 3, 500000.00, '2025-02-10 17:00:00', NULL),
('ACT-004', 'NAAC Document Submission',     'Document Submission',  'Criteria-wise SSR documents compilation.',   'high',   'Rejected', 4, 5, 4, NULL,      '2025-01-25 17:00:00', NULL),
('ACT-005', 'Hostel Maintenance Request',   'Maintenance Request',  'Plumbing repair in Block-C.',                'low',    'Closed',   6, 5, NULL, 25000.00,'2025-01-18 17:00:00', '2025-01-17 11:00:00');

-- -------------------------------------------
-- Seed data — sample audit logs
-- -------------------------------------------
INSERT INTO audit_logs (action_id, user_id, activity, details, ip_address, session_id, role) VALUES
(1, 5, 'ACTION_CREATED',   'Created action ACT-001',          '192.168.1.10', 'SES_ABC123', 'Member'),
(1, 2, 'STATUS_CHANGED',   'Status changed to Review',        '192.168.1.20', 'SES_DEF456', 'Head'),
(2, 5, 'ACTION_CREATED',   'Created action ACT-002',          '192.168.1.10', 'SES_ABC123', 'Member'),
(2, 1, 'STATUS_CHANGED',   'Status changed to Approved',      '192.168.1.15', 'SES_GHI789', 'Head'),
(3, 5, 'ACTION_CREATED',   'Created action ACT-003',          '192.168.1.10', 'SES_ABC123', 'Member'),
(4, 5, 'ACTION_CREATED',   'Created action ACT-004',          '192.168.1.10', 'SES_ABC123', 'Member'),
(4, 4, 'STATUS_CHANGED',   'Status changed to Rejected',      '192.168.1.22', 'SES_JKL012', 'Head'),
(5, 5, 'ACTION_CREATED',   'Created action ACT-005',          '192.168.1.10', 'SES_ABC123', 'Member'),
(5, 5, 'STATUS_CHANGED',   'Status changed to Closed',        '192.168.1.10', 'SES_ABC123', 'Member');
