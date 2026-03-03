<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>MicroAudit — Audit Logs</title>
    <link href="https://fonts.googleapis.com/css2?family=Fraunces:ital,wght@0,300;0,400;1,300;1,400&family=Instrument+Sans:wght@400;500;600;700&display=swap" rel="stylesheet"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard.css"/>
    <style>
        .log-activity { display:inline-flex; padding:3px 10px; border-radius:20px; font-size:11px; font-weight:600; }
        .log-login    { background:#eff6ff; color:#2563eb; }
        .log-action   { background:#f0fdf4; color:#16a34a; }
        .log-status   { background:#fffbeb; color:#d97706; }
        .log-http     { background:#f5f5f4; color:#78716c; }
        .log-failed   { background:#fef2f2; color:#dc2626; }
        .log-profile  { background:#f5f3ff; color:#7c3aed; }
        .search-bar { display:flex; gap:12px; padding:16px 22px; border-bottom:1px solid var(--border); }
        .search-bar input { flex:1; }
        .log-count { font-size:12px; color:var(--muted); padding:12px 22px; border-bottom:1px solid var(--border); }
    </style>
</head>
<body>
    <!-- Sidebar -->
    <aside class="sidebar">
        <div class="logo-wrap">
            <div class="logo-text">Micro<span>Audit</span></div>
            <div class="logo-sub">Admin Console</div>
        </div>
        <nav class="nav">
            <div class="nav-label">Admin</div>
            <a href="${pageContext.request.contextPath}/app/admin/dashboard" class="nav-item"><span class="nav-icon">📊</span> Dashboard</a>
            <a href="${pageContext.request.contextPath}/app/admin/audit-logs" class="nav-item active"><span class="nav-icon">🔍</span> Audit Logs</a>
            <a href="${pageContext.request.contextPath}/app/admin/analytics" class="nav-item"><span class="nav-icon">📈</span> Analytics</a>
            <div class="nav-label">System</div>
            <a href="${pageContext.request.contextPath}/pages/dashboard.html" class="nav-item"><span class="nav-icon">⚡</span> Member View</a>
            <a href="${pageContext.request.contextPath}/index.html" class="nav-item" onclick="sessionStorage.clear()"><span class="nav-icon">🚪</span> Logout</a>
        </nav>
        <div class="sidebar-foot">
            <div class="user-card">
                <div class="avatar">AD</div>
                <div><div class="uname">${sessionScope.userName}</div><div class="urole">${sessionScope.role}</div></div>
            </div>
        </div>
    </aside>

    <div class="main">
        <div class="topbar">
            <div class="topbar-title">Audit Logs</div>
            <div class="tb-right">
                <span class="tb-time">${totalLogs} entries</span>
            </div>
        </div>

        <div class="content">
            <div class="panel">
                <div class="ph">
                    <span class="ph-title">Complete Audit Trail</span>
                    <span style="font-size:11px;color:var(--muted)">Every action is timestamped with IP, session, and role</span>
                </div>
                <div class="log-count">
                    Showing <strong>${logs.size()}</strong> audit log entries
                </div>
                <table>
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Activity</th>
                            <th>User</th>
                            <th>Role</th>
                            <th>Action</th>
                            <th>Details</th>
                            <th>IP Address</th>
                            <th>Session</th>
                            <th>Timestamp</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${logs}" var="log">
                            <tr>
                                <td style="color:var(--muted)">#${log.id}</td>
                                <td>
                                    <span class="log-activity
                                        <c:choose>
                                            <c:when test="${log.activity == 'LOGIN_SUCCESS'}">log-login</c:when>
                                            <c:when test="${log.activity == 'LOGIN_FAILED'}">log-failed</c:when>
                                            <c:when test="${log.activity == 'ACTION_CREATED'}">log-action</c:when>
                                            <c:when test="${log.activity == 'STATUS_CHANGED'}">log-status</c:when>
                                            <c:when test="${log.activity == 'PROFILE_UPDATED'}">log-profile</c:when>
                                            <c:otherwise>log-http</c:otherwise>
                                        </c:choose>
                                    ">${log.activity}</span>
                                </td>
                                <td>${log.userName != null ? log.userName : '—'}</td>
                                <td style="color:var(--muted)">${log.role != null ? log.role : '—'}</td>
                                <td style="color:var(--accent)">${log.actionCode != null ? log.actionCode : '—'}</td>
                                <td style="font-size:11px;max-width:200px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">${log.details}</td>
                                <td style="font-family:monospace;font-size:11px;color:var(--muted)">${log.ipAddress}</td>
                                <td style="font-family:monospace;font-size:10px;color:var(--accent)">${log.sessionId != null ? log.sessionId.substring(0, Math.min(12, log.sessionId.length())) : '—'}...</td>
                                <td style="font-size:11px;color:var(--muted)">${log.createdAt}</td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</body>
</html>
