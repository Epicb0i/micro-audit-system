<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>MicroAudit — Admin Dashboard</title>
    <link href="https://fonts.googleapis.com/css2?family=Fraunces:ital,wght@0,300;0,400;1,300;1,400&family=Instrument+Sans:wght@400;500;600;700&display=swap" rel="stylesheet"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard.css"/>
    <style>
        .score-badge { display:inline-flex; padding:4px 12px; border-radius:20px; font-size:12px; font-weight:600; }
        .score-good { background:#f0fdf4; color:#16a34a; }
        .score-warn { background:#fffbeb; color:#d97706; }
        .score-bad  { background:#fef2f2; color:#dc2626; }
        .pattern-list { padding:16px 22px; }
        .pattern-item { padding:10px 0; border-bottom:1px solid var(--border); font-size:13px; color:var(--text2); line-height:1.6; }
        .pattern-item:last-child { border-bottom:none; }
        .health-score { font-family:'Fraunces',serif; font-size:48px; font-weight:300; text-align:center; padding:24px; }
        .health-good { color:#16a34a; }
        .health-warn { color:#d97706; }
        .health-bad  { color:#dc2626; }
        .metric-grid { display:grid; grid-template-columns:repeat(2,1fr); gap:12px; padding:16px 22px; }
        .metric-item { background:var(--bg); border-radius:8px; padding:14px 16px; }
        .metric-label { font-size:11px; font-weight:600; text-transform:uppercase; color:var(--muted); letter-spacing:0.5px; }
        .metric-val { font-family:'Fraunces',serif; font-size:20px; font-weight:300; color:var(--navy); margin-top:4px; }
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
            <a href="${pageContext.request.contextPath}/app/admin/dashboard" class="nav-item active"><span class="nav-icon">📊</span> Dashboard</a>
            <a href="${pageContext.request.contextPath}/app/admin/audit-logs" class="nav-item"><span class="nav-icon">🔍</span> Audit Logs</a>
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
            <div class="topbar-title">Admin Dashboard</div>
            <div class="tb-right">
                <span class="tb-time">Auditor View</span>
                <a href="${pageContext.request.contextPath}/app/admin/analytics" class="new-action-btn">📊 View Analytics</a>
            </div>
        </div>

        <div class="content">
            <!-- KPIs -->
            <div class="kpi-grid">
                <div class="kpi c1">
                    <div class="kpi-lbl">Total Actions</div>
                    <div class="kpi-val">${totalActions}</div>
                    <div class="kpi-sub">Across all committees</div>
                    <div class="kpi-trend">📋</div>
                </div>
                <div class="kpi c2">
                    <div class="kpi-lbl">Delayed</div>
                    <div class="kpi-val">${delayedCount}</div>
                    <div class="kpi-sub">Require attention</div>
                    <div class="kpi-trend">⏰</div>
                </div>
                <div class="kpi c3">
                    <div class="kpi-lbl">Committees</div>
                    <div class="kpi-val">${committees.size()}</div>
                    <div class="kpi-sub">Active committees</div>
                    <div class="kpi-trend">🏛️</div>
                </div>
                <div class="kpi c4">
                    <div class="kpi-lbl">Audit Logs</div>
                    <div class="kpi-val">${auditLogCount}</div>
                    <div class="kpi-sub">Total entries</div>
                    <div class="kpi-trend">📝</div>
                </div>
            </div>

            <div class="two-col">
                <!-- Committee Delay Analysis -->
                <div class="panel">
                    <div class="ph">
                        <span class="ph-title">Committee Delay Analysis</span>
                        <span style="font-size:11px;color:var(--muted)">Java Streams: groupingBy + averagingLong</span>
                    </div>
                    <table>
                        <thead>
                            <tr><th>Committee</th><th>Avg Delay (h)</th><th>Status</th></tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${avgDelayByCommittee}" var="entry">
                                <tr>
                                    <td>${entry.key}</td>
                                    <td style="font-weight:600">
                                        <fmt:formatNumber value="${entry.value}" maxFractionDigits="1"/>h
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${entry.value < 12}">
                                                <span class="score-badge score-good">On Track</span>
                                            </c:when>
                                            <c:when test="${entry.value < 36}">
                                                <span class="score-badge score-warn">Moderate</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="score-badge score-bad">Critical</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>

                <!-- Member Accountability Scores -->
                <div class="panel">
                    <div class="ph">
                        <span class="ph-title">Member Accountability</span>
                        <span style="font-size:11px;color:var(--muted)">Score: 0-100</span>
                    </div>
                    <table>
                        <thead>
                            <tr><th>Member</th><th>Score</th><th>Status</th></tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${memberScores}" var="entry">
                                <tr>
                                    <td>${entry.key}</td>
                                    <td style="font-weight:600">
                                        <fmt:formatNumber value="${entry.value}" maxFractionDigits="1"/>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${entry.value >= 80}">
                                                <span class="score-badge score-good">Good</span>
                                            </c:when>
                                            <c:when test="${entry.value >= 50}">
                                                <span class="score-badge score-warn">Needs Improvement</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="score-badge score-bad">Poor</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Top Delayed Actions -->
            <div class="panel">
                <div class="ph">
                    <span class="ph-title">Top Delayed Actions</span>
                    <span style="font-size:11px;color:var(--muted)">Stream.sorted + limit</span>
                </div>
                <table>
                    <thead>
                        <tr><th>Code</th><th>Title</th><th>Committee</th><th>Status</th><th>Delay</th></tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${topDelayed}" var="action">
                            <tr>
                                <td style="color:var(--accent);font-weight:600">${action.actionCode}</td>
                                <td>${action.title}</td>
                                <td style="color:var(--muted)">${action.committeeName}</td>
                                <td>
                                    <span class="status-badge
                                        <c:choose>
                                            <c:when test="${action.status == 'Created'}">s-created</c:when>
                                            <c:when test="${action.status == 'Review'}">s-review</c:when>
                                            <c:when test="${action.status == 'Approved'}">s-approved</c:when>
                                            <c:when test="${action.status == 'Rejected'}">s-rejected</c:when>
                                            <c:otherwise>s-closed</c:otherwise>
                                        </c:choose>
                                    ">${action.status}</span>
                                </td>
                                <td style="color:var(--danger);font-weight:600">${action.delayHours}h</td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>

            <!-- Max Delayed Action -->
            <c:if test="${maxDelayed != null}">
                <div class="panel" style="border-color:var(--danger);">
                    <div class="ph" style="background:#fef2f2;">
                        <span class="ph-title" style="color:var(--danger)">🚨 Maximum Delay: ${maxDelayed.actionCode}</span>
                    </div>
                    <div style="padding:20px 22px;">
                        <p style="font-size:14px;font-weight:500;color:var(--text);">${maxDelayed.title}</p>
                        <p style="font-size:12px;color:var(--muted);margin-top:6px;">
                            Committee: ${maxDelayed.committeeName} |
                            Delay: <strong style="color:var(--danger)">${maxDelayed.delayHours} hours</strong> |
                            Status: ${maxDelayed.status}
                        </p>
                    </div>
                </div>
            </c:if>
        </div>
    </div>
</body>
</html>
