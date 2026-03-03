package com.microaudit.dao;

import com.microaudit.model.Action;
import com.microaudit.util.DBUtil;
import com.microaudit.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ActionDAO — Data access for actions table.
 * Demonstrates both JDBC (PreparedStatement) and Hibernate approaches.
 * Covers: JDBC CRUD, PreparedStatement, Hibernate ORM
 */
public class ActionDAO {

    // ══════════════════════════════════════════
    //  JDBC — PreparedStatement
    // ══════════════════════════════════════════

    /**
     * Insert a new action via JDBC. Returns generated ID.
     */
    public int createActionJDBC(String actionCode, String title, String type, String description,
                                String priority, int committeeId, int createdById,
                                Integer approverId, Double estimatedCost, Timestamp expectedClose) {
        String sql = "INSERT INTO actions (action_code, title, type, description, priority, status, " +
                     "committee_id, created_by, approver_id, estimated_cost, expected_close) " +
                     "VALUES (?, ?, ?, ?, ?, 'Created', ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, actionCode);
            ps.setString(2, title);
            ps.setString(3, type);
            ps.setString(4, description);
            ps.setString(5, priority);
            ps.setInt(6, committeeId);
            ps.setInt(7, createdById);
            if (approverId != null) ps.setInt(8, approverId); else ps.setNull(8, Types.INTEGER);
            if (estimatedCost != null) ps.setDouble(9, estimatedCost); else ps.setNull(9, Types.DECIMAL);
            ps.setTimestamp(10, expectedClose);

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Fetch all actions via JDBC (with committee and user joins).
     */
    public List<ActionRow> findAllJDBC() {
        List<ActionRow> rows = new ArrayList<>();
        String sql = "SELECT a.*, c.name AS committee_name, u.name AS creator_name, u.staff_id AS creator_staff_id " +
                     "FROM actions a " +
                     "JOIN committees c ON a.committee_id = c.id " +
                     "JOIN users u ON a.created_by = u.id " +
                     "ORDER BY a.created_at DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rows.add(mapActionRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    /**
     * Fetch actions by committee via JDBC.
     */
    public List<ActionRow> findByCommitteeJDBC(int committeeId) {
        List<ActionRow> rows = new ArrayList<>();
        String sql = "SELECT a.*, c.name AS committee_name, u.name AS creator_name, u.staff_id AS creator_staff_id " +
                     "FROM actions a " +
                     "JOIN committees c ON a.committee_id = c.id " +
                     "JOIN users u ON a.created_by = u.id " +
                     "WHERE a.committee_id = ? ORDER BY a.created_at DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, committeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapActionRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    /**
     * Fetch actions by user via JDBC.
     */
    public List<ActionRow> findByUserJDBC(int userId) {
        List<ActionRow> rows = new ArrayList<>();
        String sql = "SELECT a.*, c.name AS committee_name, u.name AS creator_name, u.staff_id AS creator_staff_id " +
                     "FROM actions a " +
                     "JOIN committees c ON a.committee_id = c.id " +
                     "JOIN users u ON a.created_by = u.id " +
                     "WHERE a.created_by = ? ORDER BY a.created_at DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapActionRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    /**
     * Update action status via JDBC.
     */
    public boolean updateStatusJDBC(int actionId, String newStatus) {
        String sql = "UPDATE actions SET status=?, updated_at=CURRENT_TIMESTAMP" +
                     (newStatus.equals("Closed") || newStatus.equals("Approved") ? ", actual_close=CURRENT_TIMESTAMP" : "") +
                     " WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, actionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Count actions by status via JDBC.
     */
    public int countByStatusJDBC(String status) {
        String sql = "SELECT COUNT(*) FROM actions WHERE status=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private ActionRow mapActionRow(ResultSet rs) throws SQLException {
        ActionRow row = new ActionRow();
        row.id = rs.getInt("id");
        row.actionCode = rs.getString("action_code");
        row.title = rs.getString("title");
        row.type = rs.getString("type");
        row.description = rs.getString("description");
        row.priority = rs.getString("priority");
        row.status = rs.getString("status");
        row.committeeName = rs.getString("committee_name");
        row.creatorName = rs.getString("creator_name");
        row.creatorStaffId = rs.getString("creator_staff_id");
        row.estimatedCost = rs.getDouble("estimated_cost");
        row.expectedClose = rs.getTimestamp("expected_close");
        row.actualClose = rs.getTimestamp("actual_close");
        row.createdAt = rs.getTimestamp("created_at");
        row.committeeId = rs.getInt("committee_id");
        return row;
    }

    // ══════════════════════════════════════════
    //  Hibernate ORM
    // ══════════════════════════════════════════

    /**
     * Fetch all actions via Hibernate (with eager joins).
     */
    public List<Action> findAllHibernate() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Action a JOIN FETCH a.committee JOIN FETCH a.createdBy ORDER BY a.createdAt DESC",
                    Action.class).list();
        }
    }

    /**
     * Save a new action via Hibernate.
     */
    public void saveHibernate(Action action) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(action);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    /**
     * Find action by ID via Hibernate.
     */
    public Action findByIdHibernate(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Action a JOIN FETCH a.committee JOIN FETCH a.createdBy WHERE a.id = :id",
                    Action.class).setParameter("id", id).uniqueResult();
        }
    }

    /**
     * Update action status via Hibernate.
     */
    public void updateStatusHibernate(int actionId, Action.Status newStatus) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Action action = session.get(Action.class, actionId);
            if (action != null) {
                action.setStatus(newStatus);
                if (newStatus == Action.Status.Closed || newStatus == Action.Status.Approved) {
                    action.setActualClose(java.time.LocalDateTime.now());
                }
                session.merge(action);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════
    //  DTO for JDBC result rows
    // ══════════════════════════════════════════

    public static class ActionRow {
        public int id;
        public String actionCode;
        public String title;
        public String type;
        public String description;
        public String priority;
        public String status;
        public String committeeName;
        public String creatorName;
        public String creatorStaffId;
        public double estimatedCost;
        public Timestamp expectedClose;
        public Timestamp actualClose;
        public Timestamp createdAt;
        public int committeeId;

        /**
         * Compute delay hours for this row.
         */
        public long getDelayHours() {
            Timestamp closeRef = (actualClose != null) ? actualClose : new Timestamp(System.currentTimeMillis());
            if ("Closed".equals(status) || "Approved".equals(status)) {
                closeRef = (actualClose != null) ? actualClose : expectedClose;
            }
            long diff = closeRef.getTime() - expectedClose.getTime();
            long hours = diff / 3600000;
            return Math.max(0, hours);
        }

        public boolean isDelayed() { return getDelayHours() > 0; }
    }
}
