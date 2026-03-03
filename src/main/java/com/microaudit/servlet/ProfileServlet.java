package com.microaudit.servlet;

import com.microaudit.dao.UserDAO;
import com.microaudit.dao.AuditLogDAO;
import com.microaudit.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * ProfileServlet — Handles profile fetch and update.
 * Covers: Servlet handling, JDBC UPDATE, Session management
 */
public class ProfileServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("staffId") == null) {
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", "Not authenticated.");
            resp.setStatus(401);
            out.print(gson.toJson(err));
            return;
        }

        String staffId = (String) session.getAttribute("staffId");
        User user = userDAO.findByStaffIdJDBC(staffId);

        if (user != null) {
            JsonObject data = new JsonObject();
            data.addProperty("success", true);
            data.addProperty("staffId", user.getStaffId());
            data.addProperty("name", user.getName());
            data.addProperty("email", user.getEmail());
            data.addProperty("phone", user.getPhone());
            data.addProperty("role", user.getRole().name());
            data.addProperty("department", user.getDepartment());
            out.print(gson.toJson(data));
        } else {
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", "User not found.");
            resp.setStatus(404);
            out.print(gson.toJson(err));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        JsonObject json = new JsonObject();

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("staffId") == null) {
            json.addProperty("success", false);
            json.addProperty("message", "Not authenticated.");
            resp.setStatus(401);
            out.print(gson.toJson(json));
            return;
        }

        String staffId = (String) session.getAttribute("staffId");
        int userId = (int) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        String name = req.getParameter("fullName");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");
        String department = req.getParameter("department");

        boolean updated = userDAO.updateProfileJDBC(staffId, name, email, phone, department);

        if (updated) {
            // Update session
            session.setAttribute("userName", name);
            session.setAttribute("department", department);

            // Audit log
            auditLogDAO.insertLogJDBC(null, userId, "PROFILE_UPDATED",
                    "Profile updated by " + staffId,
                    req.getRemoteAddr(), session.getId(), role);

            json.addProperty("success", true);
            json.addProperty("message", "Profile updated successfully.");
        } else {
            json.addProperty("success", false);
            json.addProperty("message", "Failed to update profile.");
            resp.setStatus(500);
        }

        out.print(gson.toJson(json));
    }
}
