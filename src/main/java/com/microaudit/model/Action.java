package com.microaudit.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Action entity — maps to the 'actions' table.
 * Represents a committee action (approval, purchase, event, sanction).
 * Lifecycle: Created → Review → Approved / Rejected → Closed
 * Covers: Hibernate ORM, Java compute logic
 */
@Entity
@Table(name = "actions")
public class Action {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "action_code", nullable = false, unique = true)
    private String actionCode;

    @Column(nullable = false)
    private String title;

    private String type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "committee_id", nullable = false)
    private Committee committee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private User approver;

    @Column(name = "estimated_cost")
    private BigDecimal estimatedCost;

    @Column(name = "expected_close", nullable = false)
    private LocalDateTime expectedClose;

    @Column(name = "actual_close")
    private LocalDateTime actualClose;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Enums ─────────────────────────────────
    public enum Priority { low, medium, high }

    public enum Status { Created, Review, Approved, Rejected, Closed }

    // ── Constructors ──────────────────────────
    public Action() {
        this.status = Status.Created;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ── Delay Computation (CORE LOGIC) ────────
    /**
     * Calculates delay in hours.
     * Positive = delayed, Zero/negative = on time.
     */
    public long getDelayHours() {
        LocalDateTime closeRef = (actualClose != null) ? actualClose : LocalDateTime.now();
        if (status == Status.Closed || status == Status.Approved) {
            closeRef = (actualClose != null) ? actualClose : expectedClose;
        }
        long hours = Duration.between(expectedClose, closeRef).toHours();
        return Math.max(0, hours);
    }

    /**
     * Returns delay in minutes for fine-grained analysis.
     */
    public long getDelayMinutes() {
        LocalDateTime closeRef = (actualClose != null) ? actualClose : LocalDateTime.now();
        if (status == Status.Closed || status == Status.Approved) {
            closeRef = (actualClose != null) ? actualClose : expectedClose;
        }
        long minutes = Duration.between(expectedClose, closeRef).toMinutes();
        return Math.max(0, minutes);
    }

    /**
     * Checks whether this action is delayed.
     */
    public boolean isDelayed() {
        return getDelayHours() > 0;
    }

    /**
     * Computes delay percentage relative to total lifecycle duration.
     */
    public double getDelayPercentage() {
        long totalMinutes = Duration.between(createdAt, expectedClose).toMinutes();
        if (totalMinutes <= 0) return 0.0;
        long delayMinutes = getDelayMinutes();
        return (delayMinutes * 100.0) / totalMinutes;
    }

    /**
     * Helper: returns committee name for Streams grouping.
     */
    public String getCommitteeName() {
        return (committee != null) ? committee.getName() : "Unknown";
    }

    /**
     * Helper: returns creator's staff ID for Streams grouping.
     */
    public String getCreatorStaffId() {
        return (createdBy != null) ? createdBy.getStaffId() : "Unknown";
    }

    // ── Getters / Setters ─────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getActionCode() { return actionCode; }
    public void setActionCode(String actionCode) { this.actionCode = actionCode; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; this.updatedAt = LocalDateTime.now(); }

    public Committee getCommittee() { return committee; }
    public void setCommittee(Committee committee) { this.committee = committee; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public User getApprover() { return approver; }
    public void setApprover(User approver) { this.approver = approver; }

    public BigDecimal getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }

    public LocalDateTime getExpectedClose() { return expectedClose; }
    public void setExpectedClose(LocalDateTime expectedClose) { this.expectedClose = expectedClose; }

    public LocalDateTime getActualClose() { return actualClose; }
    public void setActualClose(LocalDateTime actualClose) { this.actualClose = actualClose; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Action{code='" + actionCode + "', title='" + title + "', status=" + status + ", delay=" + getDelayHours() + "h}";
    }
}
