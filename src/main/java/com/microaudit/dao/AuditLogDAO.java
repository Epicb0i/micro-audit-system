package com.microaudit.dao;

import com.microaudit.model.AuditLog;
import com.microaudit.util.DBUtil;
import com.microaudit.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AuditLogDAO — Data access for audit_logs table.
 * Covers: JDBC SELECT queries, Hibernate ORM, JSON audit reports
 */
public class AuditLogDAO {

    // ══════════════════════════════════════════
    //  JDBC — PreparedStatement
    // ══════════════════════════════════════════

    /**
     * Insert audit log entry via JDBC.
     */
    public void insertLogJDBC(Integer actionId, Integer userId, String activity,
                              String details, String ipAddress, String sessionId, String role) {
        String sql = "INSERT INTO audit_logs (action_id, user_id, activity, details, ip_address, session_id, role) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (actionId != null) ps.setInt(1, actionId); else ps.setNull(1, Types.INTEGER);
            if (userId != null) ps.setInt(2, userId); else ps.setNull(2, Types.INTEGER);
            ps.setString(3, activity);
            ps.setString(4, details);
            ps.setString(5, ipAddress);
            ps.setString(6, sessionId);
            ps.setString(7, role);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetch all audit logs (most recent first) via JDBC.
     */
    public List<AuditLogRow> findAllJDBC() {
        List<AuditLogRow> rows = new ArrayList<>();
        String sql = "SELECT al.*, u.name AS user_name, u.staff_id AS user_staff_id, " +
                     "a.action_code, a.title AS action_title " +
                     "FROM audit_logs al " +
                     "LEFT JOIN users u ON al.user_id = u.id " +
                     "LEFT JOIN actions a ON al.action_id = a.id " +
                     "ORDER BY al.created_at DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rows.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    /**
     * Fetch audit logs for a specific action via JDBC.
     */
    public List<AuditLogRow> findByActionJDBC(int actionId) {
        List<AuditLogRow> rows = new ArrayList<>();
        String sql = "SELECT al.*, u.name AS user_name, u.staff_id AS user_staff_id, " +
                     "a.action_code, a.title AS action_title " +
                     "FROM audit_logs al " +
                     "LEFT JOIN users u ON al.user_id = u.id " +
                     "LEFT JOIN actions a ON al.action_id = a.id " +
                     "WHERE al.action_id = ? ORDER BY al.created_at DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, actionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    /**
     * Fetch audit logs for a specific user via JDBC.
     */
    public List<AuditLogRow> findByUserJDBC(int userId) {
        List<AuditLogRow> rows = new ArrayList<>();
        String sql = "SELECT al.*, u.name AS user_name, u.staff_id AS user_staff_id, " +
                     "a.action_code, a.title AS action_title " +
                     "FROM audit_logs al " +
                     "LEFT JOIN users u ON al.user_id = u.id " +
                     "LEFT JOIN actions a ON al.action_id = a.id " +
                     "WHERE al.user_id = ? ORDER BY al.created_at DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    /**
     * Count total audit log entries.
     */
    public int countAllJDBC() {
        String sql = "SELECT COUNT(*) FROM audit_logs";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private AuditLogRow mapRow(ResultSet rs) throws SQLException {
        AuditLogRow row = new AuditLogRow();
        row.id = rs.getLong("id");
        row.actionId = rs.getInt("action_id");
        row.userId = rs.getInt("user_id");
        row.activity = rs.getString("activity");
        row.details = rs.getString("details");
        row.ipAddress = rs.getString("ip_address");
        row.sessionId = rs.getString("session_id");
        row.role = rs.getString("role");
        row.createdAt = rs.getTimestamp("created_at");
        row.userName = rs.getString("user_name");
        row.userStaffId = rs.getString("user_staff_id");
        row.actionCode = rs.getString("action_code");
        row.actionTitle = rs.getString("action_title");
        return row;
    }

    // ══════════════════════════════════════════
    //  Hibernate ORM
    // ══════════════════════════════════════════

    /**
     * Save audit log via Hibernate.
     */
    public void saveHibernate(AuditLog log) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(log);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    /**
     * Fetch all audit logs via Hibernate.
     */
    public List<AuditLog> findAllHibernate() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM AuditLog ORDER BY createdAt DESC", AuditLog.class).list();
        }
    }

    // ══════════════════════════════════════════
    //  DTO for JDBC result rows
    // ══════════════════════════════════════════

    public static class AuditLogRow {
        public long id;
        public int actionId;
        public int userId;
        public String activity;
        public String details;
        public String ipAddress;
        public String sessionId;
        public String role;
        public Timestamp createdAt;
        public String userName;
        public String userStaffId;
        public String actionCode;
        public String actionTitle;
    }
}
