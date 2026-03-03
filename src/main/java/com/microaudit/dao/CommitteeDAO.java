package com.microaudit.dao;

import com.microaudit.model.Committee;
import com.microaudit.util.DBUtil;
import com.microaudit.util.HibernateUtil;
import org.hibernate.Session;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CommitteeDAO — Data access for committees table.
 * Covers: JDBC + Hibernate
 */
public class CommitteeDAO {

    /**
     * Fetch all committees via JDBC.
     */
    public List<Committee> findAllJDBC() {
        List<Committee> list = new ArrayList<>();
        String sql = "SELECT * FROM committees ORDER BY id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Committee c = new Committee();
                c.setId(rs.getInt("id"));
                c.setName(rs.getString("name"));
                c.setDescription(rs.getString("description"));
                list.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Find committee by ID via JDBC.
     */
    public Committee findByIdJDBC(int id) {
        String sql = "SELECT * FROM committees WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Committee c = new Committee();
                    c.setId(rs.getInt("id"));
                    c.setName(rs.getString("name"));
                    c.setDescription(rs.getString("description"));
                    return c;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Find committee by name via JDBC.
     */
    public Committee findByNameJDBC(String name) {
        String sql = "SELECT * FROM committees WHERE name = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Committee c = new Committee();
                    c.setId(rs.getInt("id"));
                    c.setName(rs.getString("name"));
                    c.setDescription(rs.getString("description"));
                    return c;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Fetch all committees via Hibernate.
     */
    public List<Committee> findAllHibernate() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Committee ORDER BY id", Committee.class).list();
        }
    }
}
