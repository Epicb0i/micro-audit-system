package com.microaudit.dao;

import com.microaudit.model.User;
import com.microaudit.util.DBUtil;
import com.microaudit.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDAO — Data access for users table.
 * Demonstrates both JDBC (PreparedStatement) and Hibernate approaches.
 * Covers: JDBC CRUD, PreparedStatement, Hibernate ORM
 */
public class UserDAO {

    // ══════════════════════════════════════════
    //  JDBC — PreparedStatement (Prevents SQL Injection)
    // ══════════════════════════════════════════

    /**
     * Authenticate user by staffId and password using PreparedStatement.
     */
    public User authenticateJDBC(String staffId, String password) {
        String sql = "SELECT u.*, c.name AS committee_name FROM users u " +
                     "LEFT JOIN committees c ON u.committee_id = c.id " +
                     "WHERE u.staff_id = ? AND u.password = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, staffId);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Fetch all users via JDBC.
     */
    public List<User> findAllJDBC() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.*, c.name AS committee_name FROM users u " +
                     "LEFT JOIN committees c ON u.committee_id = c.id ORDER BY u.id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Find user by staff ID via JDBC.
     */
    public User findByStaffIdJDBC(String staffId) {
        String sql = "SELECT u.*, c.name AS committee_name FROM users u " +
                     "LEFT JOIN committees c ON u.committee_id = c.id " +
                     "WHERE u.staff_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Update user profile via JDBC.
     */
    public boolean updateProfileJDBC(String staffId, String name, String email, String phone, String department) {
        String sql = "UPDATE users SET name=?, email=?, phone=?, department=? WHERE staff_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, department);
            ps.setString(5, staffId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private User mapResultSet(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setStaffId(rs.getString("staff_id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setPassword(rs.getString("password"));
        u.setRole(User.Role.valueOf(rs.getString("role")));
        u.setDepartment(rs.getString("department"));
        if (rs.getTimestamp("created_at") != null) {
            u.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return u;
    }

    // ══════════════════════════════════════════
    //  Hibernate ORM
    // ══════════════════════════════════════════

    /**
     * Authenticate user via Hibernate.
     */
    public User authenticateHibernate(String staffId, String password) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM User WHERE staffId = :sid AND password = :pw", User.class)
                    .setParameter("sid", staffId)
                    .setParameter("pw", password)
                    .uniqueResult();
        }
    }

    /**
     * Fetch all users via Hibernate.
     */
    public List<User> findAllHibernate() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM User ORDER BY id", User.class).list();
        }
    }

    /**
     * Find user by ID via Hibernate.
     */
    public User findByIdHibernate(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(User.class, id);
        }
    }

    /**
     * Save or update user via Hibernate.
     */
    public void saveHibernate(User user) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(user);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }
}
