package com.microaudit.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * AuditLog entity — maps to the 'audit_logs' table.
 * Every action generates: who did it, timestamp, role, IP/session ID.
 * Covers: Hibernate ORM, Servlet Filter logging
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id")
    private Action action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String activity;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "session_id")
    private String sessionId;

    private String role;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // ── Constructors ──────────────────────────
    public AuditLog() {
        this.createdAt = LocalDateTime.now();
    }

    public AuditLog(Action action, User user, String activity, String details,
                    String ipAddress, String sessionId, String role) {
        this.action = action;
        this.user = user;
        this.activity = activity;
        this.details = details;
        this.ipAddress = ipAddress;
        this.sessionId = sessionId;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    // ── Getters / Setters ─────────────────────
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public Action getAction() { return action; }
    public void setAction(Action action) { this.action = action; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getActivity() { return activity; }
    public void setActivity(String activity) { this.activity = activity; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "AuditLog{id=" + id + ", activity='" + activity + "', user=" + (user != null ? user.getStaffId() : "null") + "}";
    }
}
