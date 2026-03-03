package com.microaudit.servlet;

import com.microaudit.dao.ActionDAO;
import com.microaudit.dao.AuditLogDAO;
import com.microaudit.dao.CommitteeDAO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * DashboardServlet — Provides dashboard data (KPIs, actions, committees).
 * Covers: Servlet handling, JDBC SELECT, Session management
 */
public class DashboardServlet extends HttpServlet {

    private final ActionDAO actionDAO = new ActionDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();
    private final CommitteeDAO committeeDAO = new CommitteeDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        // Session check
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", "Not authenticated.");
            resp.setStatus(401);
            out.print(gson.toJson(err));
            return;
        }

        String section = req.getParameter("section"); // "kpi", "actions", "committees", or null (all)

        JsonObject data = new JsonObject();
        data.addProperty("success", true);

        if (section == null || "kpi".equals(section)) {
            // KPI data
            List<ActionDAO.ActionRow> allActions = actionDAO.findAllJDBC();
            int total = allActions.size();
            long delayed = allActions.stream().filter(ActionDAO.ActionRow::isDelayed).count();
            long approved = allActions.stream().filter(a -> "Approved".equals(a.status) || "Closed".equals(a.status)).count();
            double avgDelay = allActions.stream().mapToLong(ActionDAO.ActionRow::getDelayHours).average().orElse(0);
            int auditCount = auditLogDAO.countAllJDBC();

            JsonObject kpi = new JsonObject();
            kpi.addProperty("total", total);
            kpi.addProperty("delayed", delayed);
            kpi.addProperty("approved", approved);
            kpi.addProperty("avgDelay", Math.round(avgDelay));
            kpi.addProperty("auditLogs", auditCount);
            data.add("kpi", kpi);
        }

        if (section == null || "actions".equals(section)) {
            data.add("actions", gson.toJsonTree(actionDAO.findAllJDBC()));
        }

        if (section == null || "committees".equals(section)) {
            data.add("committees", gson.toJsonTree(committeeDAO.findAllJDBC()));
        }

        out.print(gson.toJson(data));
    }
}
