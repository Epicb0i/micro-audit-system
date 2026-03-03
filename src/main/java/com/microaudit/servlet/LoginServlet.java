package com.microaudit.servlet;

import com.microaudit.dao.AuditLogDAO;
import com.microaudit.dao.UserDAO;
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
 * LoginServlet — Handles authentication via POST.
 * Validates credentials, creates session, logs audit entry.
 * Covers: Servlet handling, Sessions & Cookies
 */
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        String staffId = req.getParameter("userId");
        String password = req.getParameter("password");
        String role = req.getParameter("role");

        JsonObject json = new JsonObject();

        // Validate input
        if (staffId == null || staffId.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            json.addProperty("success", false);
            json.addProperty("message", "Staff ID and password are required.");
            resp.setStatus(400);
            out.print(gson.toJson(json));
            return;
        }

        // Authenticate using JDBC PreparedStatement (prevents SQL injection)
        User user = userDAO.authenticateJDBC(staffId.trim(), password.trim());

        if (user == null) {
            json.addProperty("success", false);
            json.addProperty("message", "Invalid credentials. Please try again.");
            resp.setStatus(401);

            // Log failed attempt
            auditLogDAO.insertLogJDBC(null, null, "LOGIN_FAILED",
                    "Failed login attempt for: " + staffId,
                    req.getRemoteAddr(), null, role);

            out.print(gson.toJson(json));
            return;
        }

        // Create session
        HttpSession session = req.getSession(true);
        session.setAttribute("userId", user.getId());
        session.setAttribute("staffId", user.getStaffId());
        session.setAttribute("userName", user.getName());
        session.setAttribute("role", user.getRole().name());
        session.setAttribute("department", user.getDepartment());
        session.setMaxInactiveInterval(30 * 60); // 30 minutes

        // Log successful login
        auditLogDAO.insertLogJDBC(null, user.getId(), "LOGIN_SUCCESS",
                "User logged in: " + user.getStaffId(),
                req.getRemoteAddr(), session.getId(), user.getRole().name());

        // Build response
        json.addProperty("success", true);
        json.addProperty("message", "Login successful");
        json.addProperty("staffId", user.getStaffId());
        json.addProperty("name", user.getName());
        json.addProperty("role", user.getRole().name());
        json.addProperty("sessionId", session.getId());

        out.print(gson.toJson(json));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Redirect GET to login page
        resp.sendRedirect(req.getContextPath() + "/index.html");
    }
}
