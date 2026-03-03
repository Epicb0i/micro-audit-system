package com.microaudit.util;

import com.microaudit.dao.AuditLogDAO;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * AuditFilter — Intercepts all HTTP requests and logs them.
 * Every request generates: who did it, timestamp, role, IP, session ID.
 * Covers: Servlet Filters (logging) practical
 */
public class AuditFilter implements Filter {

    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("[AuditFilter] Initialized — logging all requests.");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        long startTime = System.currentTimeMillis();

        // Extract info
        String uri = httpReq.getRequestURI();
        String method = httpReq.getMethod();
        String ip = httpReq.getRemoteAddr();

        // Skip static resources (CSS, JS, images)
        if (uri.endsWith(".css") || uri.endsWith(".js") || uri.endsWith(".png") ||
            uri.endsWith(".jpg") || uri.endsWith(".ico") || uri.endsWith(".woff2")) {
            chain.doFilter(request, response);
            return;
        }

        // Extract user info from session (if logged in)
        HttpSession session = httpReq.getSession(false);
        Integer userId = null;
        String role = "Anonymous";
        String sessionId = "N/A";

        if (session != null) {
            userId = (Integer) session.getAttribute("userId");
            String sessionRole = (String) session.getAttribute("role");
            if (sessionRole != null) role = sessionRole;
            sessionId = session.getId();
        }

        // Continue filter chain
        chain.doFilter(request, response);

        // Calculate processing time
        long duration = System.currentTimeMillis() - startTime;

        // Log to audit trail
        String activity = "HTTP_REQUEST";
        String details = method + " " + uri + " [" + duration + "ms]";

        try {
            auditLogDAO.insertLogJDBC(null, userId, activity, details, ip, sessionId, role);
        } catch (Exception e) {
            // Don't let filter errors break the application
            System.err.println("[AuditFilter] Failed to log: " + e.getMessage());
        }

        System.out.println("[AuditFilter] " + method + " " + uri + " | " + role + " | " + duration + "ms");
    }

    @Override
    public void destroy() {
        System.out.println("[AuditFilter] Destroyed.");
    }
}
