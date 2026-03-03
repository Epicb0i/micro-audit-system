package com.microaudit.servlet;

import com.microaudit.dao.AuditLogDAO;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * AuditLogServlet — Provides audit log data as JSON.
 * Covers: JDBC SELECT queries, JSON audit reports
 */
public class AuditLogServlet extends HttpServlet {

    private final AuditLogDAO auditLogDAO = new AuditLogDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.setStatus(401);
            out.print("{\"success\":false,\"message\":\"Not authenticated.\"}");
            return;
        }

        String filter = req.getParameter("filter"); // "action", "user", or null (all)
        String idParam = req.getParameter("id");

        List<AuditLogDAO.AuditLogRow> logs;
        if ("action".equals(filter) && idParam != null) {
            logs = auditLogDAO.findByActionJDBC(Integer.parseInt(idParam));
        } else if ("user".equals(filter) && idParam != null) {
            logs = auditLogDAO.findByUserJDBC(Integer.parseInt(idParam));
        } else {
            logs = auditLogDAO.findAllJDBC();
        }

        out.print(gson.toJson(logs));
    }
}
