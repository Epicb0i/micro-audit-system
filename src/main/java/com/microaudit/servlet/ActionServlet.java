package com.microaudit.servlet;

import com.microaudit.dao.ActionDAO;
import com.microaudit.dao.AuditLogDAO;
import com.microaudit.dao.CommitteeDAO;
import com.microaudit.model.Committee;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * ActionServlet — Handles action CRUD operations.
 * POST  = Create new action
 * GET   = Fetch action(s)
 * PUT   = Update action status
 * Covers: Servlet form handling, JDBC INSERT, Session management
 */
public class ActionServlet extends HttpServlet {

    private final ActionDAO actionDAO = new ActionDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();
    private final CommitteeDAO committeeDAO = new CommitteeDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        JsonObject json = new JsonObject();

        // Session check
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            json.addProperty("success", false);
            json.addProperty("message", "Not authenticated. Please login.");
            resp.setStatus(401);
            out.print(gson.toJson(json));
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        String staffId = (String) session.getAttribute("staffId");

        // Extract form params
        String title = req.getParameter("title");
        String type = req.getParameter("type");
        String committeeName = req.getParameter("committee");
        String dueDateStr = req.getParameter("dueDate");
        String description = req.getParameter("description");
        String priority = req.getParameter("priority");
        String costStr = req.getParameter("cost");
        String approver = req.getParameter("approver");

        // Validate required fields
        if (title == null || title.trim().isEmpty() || dueDateStr == null || dueDateStr.trim().isEmpty()) {
            json.addProperty("success", false);
            json.addProperty("message", "Title and due date are required.");
            resp.setStatus(400);
            out.print(gson.toJson(json));
            return;
        }

        // Resolve committee ID
        int committeeId = 1; // default
        if (committeeName != null && !committeeName.isEmpty()) {
            Committee c = committeeDAO.findByNameJDBC(committeeName);
            if (c != null) committeeId = c.getId();
        }

        // Parse due date
        LocalDate dueDate = LocalDate.parse(dueDateStr);
        Timestamp expectedClose = Timestamp.valueOf(LocalDateTime.of(dueDate, LocalTime.of(17, 0)));

        // Parse cost
        Double cost = null;
        if (costStr != null && !costStr.trim().isEmpty()) {
            try { cost = Double.parseDouble(costStr); } catch (NumberFormatException ignored) {}
        }

        // Generate action code
        String actionCode = "ACT-" + System.currentTimeMillis();

        // Insert via JDBC PreparedStatement
        int newId = actionDAO.createActionJDBC(actionCode, title.trim(), type, description,
                priority != null ? priority : "low", committeeId, userId,
                null, cost, expectedClose);

        if (newId > 0) {
            // Log audit entry
            auditLogDAO.insertLogJDBC(newId, userId, "ACTION_CREATED",
                    "Created action " + actionCode + ": " + title,
                    req.getRemoteAddr(), session.getId(), role);

            json.addProperty("success", true);
            json.addProperty("message", "Action created successfully");
            json.addProperty("actionCode", actionCode);
            json.addProperty("actionId", newId);
        } else {
            json.addProperty("success", false);
            json.addProperty("message", "Failed to create action. Please try again.");
            resp.setStatus(500);
        }

        out.print(gson.toJson(json));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        String filter = req.getParameter("filter"); // "all", "user", "committee"
        String idParam = req.getParameter("id");

        if ("user".equals(filter) && idParam != null) {
            out.print(gson.toJson(actionDAO.findByUserJDBC(Integer.parseInt(idParam))));
        } else if ("committee".equals(filter) && idParam != null) {
            out.print(gson.toJson(actionDAO.findByCommitteeJDBC(Integer.parseInt(idParam))));
        } else {
            out.print(gson.toJson(actionDAO.findAllJDBC()));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        JsonObject json = new JsonObject();

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            json.addProperty("success", false);
            json.addProperty("message", "Not authenticated.");
            resp.setStatus(401);
            out.print(gson.toJson(json));
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        String actionIdStr = req.getParameter("actionId");
        String newStatus = req.getParameter("status");

        if (actionIdStr == null || newStatus == null) {
            json.addProperty("success", false);
            json.addProperty("message", "actionId and status are required.");
            resp.setStatus(400);
            out.print(gson.toJson(json));
            return;
        }

        int actionId = Integer.parseInt(actionIdStr);
        boolean updated = actionDAO.updateStatusJDBC(actionId, newStatus);

        if (updated) {
            auditLogDAO.insertLogJDBC(actionId, userId, "STATUS_CHANGED",
                    "Status changed to " + newStatus,
                    req.getRemoteAddr(), session.getId(), role);

            json.addProperty("success", true);
            json.addProperty("message", "Status updated to " + newStatus);
        } else {
            json.addProperty("success", false);
            json.addProperty("message", "Update failed.");
            resp.setStatus(500);
        }

        out.print(gson.toJson(json));
    }
}
