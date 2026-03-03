package com.microaudit.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * User entity — maps to the 'users' table.
 * Covers: Hibernate ORM (Entity, Id, GeneratedValue)
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "staff_id", nullable = false, unique = true)
    private String staffId;

    @Column(nullable = false)
    private String name;

    private String email;
    private String phone;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "committee_id")
    private Committee committee;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum Role {
        Member, Head, Auditor
    }

    // ── Constructors ──────────────────────────
    public User() {}

    public User(String staffId, String name, String password, Role role) {
        this.staffId = staffId;
        this.name = name;
        this.password = password;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    // ── Getters / Setters ─────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Committee getCommittee() { return committee; }
    public void setCommittee(Committee committee) { this.committee = committee; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "User{id=" + id + ", staffId='" + staffId + "', name='" + name + "', role=" + role + "}";
    }
}
