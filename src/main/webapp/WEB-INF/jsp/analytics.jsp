<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>MicroAudit — Analytics</title>
    <link href="https://fonts.googleapis.com/css2?family=Fraunces:ital,wght@0,300;0,400;1,300;1,400&family=Instrument+Sans:wght@400;500;600;700&display=swap" rel="stylesheet"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard.css"/>
    <style>
        .analytics-grid { display:grid; grid-template-columns:1fr 1fr; gap:20px; }
        .full-width { grid-column: 1 / -1; }
        .score-badge { display:inline-flex; padding:4px 12px; border-radius:20px; font-size:12px; font-weight:600; }
        .score-good { background:#f0fdf4; color:#16a34a; }
        .score-warn { background:#fffbeb; color:#d97706; }
        .score-bad  { background:#fef2f2; color:#dc2626; }
        .pattern-list { padding:16px 22px; }
        .pattern-item { padding:10px 0; border-bottom:1px solid var(--border); font-size:13px; color:var(--text2); line-height:1.6; }
        .pattern-item:last-child { border-bottom:none; }
        .code-block { background:#1a2744; color:#93c5fd; padding:20px; border-radius:8px; font-family:monospace; font-size:12px; line-height:1.8; overflow-x:auto; margin:16px 22px; }
        .code-comment { color:#6b7280; }
        .code-keyword { color:#f472b6; }
        .code-method  { color:#fbbf24; }
        .code-string  { color:#34d399; }
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
            <a href="${pageContext.request.contextPath}/app/admin/audit-logs" class="nav-item"><span class="nav-icon">🔍</span> Audit Logs</a>
            <a href="${pageContext.request.contextPath}/app/admin/analytics" class="nav-item active"><span class="nav-icon">📈</span> Analytics</a>
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
            <div class="topbar-title">Analytics & Reports</div>
            <div class="tb-right">
                <span class="tb-time">Java Streams API</span>
            </div>
        </div>

        <div class="content">
            <div class="analytics-grid">
                <!-- Average Delay by Committee -->
                <div class="panel">
                    <div class="ph">
                        <span class="ph-title">Avg Delay by Committee</span>
                        <span style="font-size:11px;color:var(--muted)">groupingBy + averagingLong</span>
                    </div>
                    <table>
                        <thead><tr><th>Committee</th><th>Avg Delay</th></tr></thead>
                        <tbody>
                            <c:forEach items="${avgDelayByCommittee}" var="entry">
                                <tr>
                                    <td>${entry.key}</td>
                                    <td style="font-weight:600;color:${entry.value > 24 ? 'var(--danger)' : entry.value > 6 ? 'var(--warn)' : 'var(--success)'}">
                                        <fmt:formatNumber value="${entry.value}" maxFractionDigits="1"/>h
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>

                <!-- Average Delay by Priority -->
                <div class="panel">
                    <div class="ph">
                        <span class="ph-title">Avg Delay by Priority</span>
                        <span style="font-size:11px;color:var(--muted)">groupingBy + averagingLong</span>
                    </div>
                    <table>
                        <thead><tr><th>Priority</th><th>Avg Delay</th></tr></thead>
                        <tbody>
                            <c:forEach items="${avgDelayByPriority}" var="entry">
                                <tr>
                                    <td>
                                        <c:choose>
                                            <c:when test="${entry.key == 'high'}">🔴 High</c:when>
                                            <c:when test="${entry.key == 'medium'}">🟡 Medium</c:when>
                                            <c:otherwise>🟢 Low</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="font-weight:600">
                                        <fmt:formatNumber value="${entry.value}" maxFractionDigits="1"/>h
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>

                <!-- Status Distribution -->
                <div class="panel">
                    <div class="ph">
                        <span class="ph-title">Status Distribution</span>
                        <span style="font-size:11px;color:var(--muted)">groupingBy + counting</span>
                    </div>
                    <table>
                        <thead><tr><th>Status</th><th>Count</th></tr></thead>
                        <tbody>
                            <c:forEach items="${statusDistribution}" var="entry">
                                <tr>
                                    <td>
                                        <span class="status-badge
                                            <c:choose>
                                                <c:when test="${entry.key == 'Created'}">s-created</c:when>
                                                <c:when test="${entry.key == 'Review'}">s-review</c:when>
                                                <c:when test="${entry.key == 'Approved'}">s-approved</c:when>
                                                <c:when test="${entry.key == 'Rejected'}">s-rejected</c:when>
                                                <c:otherwise>s-closed</c:otherwise>
                                            </c:choose>
                                        ">${entry.key}</span>
                                    </td>
                                    <td style="font-weight:600">${entry.value}</td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>

                <!-- Actions per Member -->
                <div class="panel">
                    <div class="ph">
                        <span class="ph-title">Actions per Member</span>
                        <span style="font-size:11px;color:var(--muted)">groupingBy + counting</span>
                    </div>
                    <table>
                        <thead><tr><th>Member (Staff ID)</th><th>Actions</th></tr></thead>
                        <tbody>
                            <c:forEach items="${countPerMember}" var="entry">
                                <tr>
                                    <td>${entry.key}</td>
                                    <td style="font-weight:600">${entry.value}</td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>

                <!-- On-Time vs Delayed (Partitioning) -->
                <div class="panel">
                    <div class="ph">
                        <span class="ph-title">On-Time vs Delayed</span>
                        <span style="font-size:11px;color:var(--muted)">partitioningBy</span>
                    </div>
                    <table>
                        <thead><tr><th>Category</th><th>Count</th></tr></thead>
                        <tbody>
                            <tr>
                                <td><span class="score-badge score-good">✅ On Time</span></td>
                                <td style="font-weight:600;color:var(--success)">
                                    ${partitioned[false] != null ? partitioned[false].size() : 0}
                                </td>
                            </tr>
                            <tr>
                                <td><span class="score-badge score-bad">⏰ Delayed</span></td>
                                <td style="font-weight:600;color:var(--danger)">
                                    ${partitioned[true] != null ? partitioned[true].size() : 0}
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <!-- Max Delayed Action -->
                <div class="panel">
                    <div class="ph">
                        <span class="ph-title">Maximum Delay Action</span>
                        <span style="font-size:11px;color:var(--muted)">Stream.max</span>
                    </div>
                    <c:if test="${maxDelayed != null}">
                        <div style="padding:20px 22px;">
                            <p style="font-size:14px;font-weight:600;color:var(--accent);">${maxDelayed.actionCode}</p>
                            <p style="font-size:13px;color:var(--text);margin-top:4px;">${maxDelayed.title}</p>
                            <p style="font-size:12px;color:var(--muted);margin-top:8px;">
                                Committee: ${maxDelayed.committeeName}<br/>
                                Delay: <strong style="color:var(--danger)">${maxDelayed.delayHours} hours</strong>
                            </p>
                        </div>
                    </c:if>
                </div>

                <!-- Delay Patterns Detection -->
                <div class="panel full-width">
                    <div class="ph">
                        <span class="ph-title">Delay Pattern Detection</span>
                        <span style="font-size:11px;color:var(--muted)">DelayDetectionEngine</span>
                    </div>
                    <div class="pattern-list">
                        <c:forEach items="${delayPatterns}" var="pattern">
                            <div class="pattern-item">${pattern}</div>
                        </c:forEach>
                    </div>
                </div>

                <!-- Java Streams Code Example (for exam reference) -->
                <div class="panel full-width">
                    <div class="ph">
                        <span class="ph-title">Streams Code Reference</span>
                        <span style="font-size:11px;color:var(--muted)">Used in StreamsAnalytics.java</span>
                    </div>
                    <div class="code-block">
<span class="code-comment">// Average delay per committee</span>
actions.<span class="code-method">stream</span>()
    .<span class="code-method">collect</span>(Collectors.<span class="code-method">groupingBy</span>(
        Action::getCommitteeName,
        Collectors.<span class="code-method">averagingLong</span>(Action::getDelayHours)
    ));

<span class="code-comment">// Partition actions (on-time vs delayed)</span>
actions.<span class="code-method">stream</span>()
    .<span class="code-method">collect</span>(Collectors.<span class="code-method">partitioningBy</span>(Action::isDelayed));

<span class="code-comment">// Find max delay action</span>
actions.<span class="code-method">stream</span>()
    .<span class="code-method">max</span>(Comparator.<span class="code-method">comparingLong</span>(Action::getDelayHours));

<span class="code-comment">// Count actions per member</span>
actions.<span class="code-method">stream</span>()
    .<span class="code-method">collect</span>(Collectors.<span class="code-method">groupingBy</span>(
        Action::getCreatorStaffId,
        Collectors.<span class="code-method">counting</span>()
    ));
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
