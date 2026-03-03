/**
 * dashboard.js
 * Renders KPIs, action table, activity feed, and delay bars.
 * Member 1 owns this file.
 * Member 2: replace sample data with data fetched from DashboardServlet.
 */

// ── Sample data (replace with fetch from Servlet) ──
const actions = [
  new Action('ACT-001', 'Equipment Purchase Approval', 'Purchase', 'Review',   '2025-02-01', '2025-02-05'),
  new Action('ACT-002', 'Exam Hall Booking',           'Exam',     'Approved',  '2025-01-28', '2025-02-02'),
  new Action('ACT-003', 'Cultural Fest Budget Sanction','Cultural', 'Created',   '2025-02-03', '2025-02-10'),
  new Action('ACT-004', 'NAAC Document Submission',    'NAAC',     'Rejected',  '2025-01-20', '2025-01-25'),
  new Action('ACT-005', 'Hostel Maintenance Request',  'Hostel',   'Closed',    '2025-01-15', '2025-01-18'),
];

// Mirrors Java Streams: Collectors.groupingBy + averagingLong(Action::getDelay)
const committees = [
  { name: 'Exam',     delay: -0.5, score: 95 },
  { name: 'Cultural', delay:  0.2, score: 89 },
  { name: 'NAAC',     delay:  0.8, score: 83 },
  { name: 'Purchase', delay:  1.5, score: 78 },
  { name: 'Hostel',   delay:  2.8, score: 57 },
  { name: 'T&P',      delay:  5.2, score: 45 },
];

const feedItems = [
  { color: 'fd-blue',   text: 'ACT-003 created by you',             time: 'Today 09:14 AM' },
  { color: 'fd-green',  text: 'ACT-002 approved by Dr. Sharma',     time: 'Yesterday 04:30 PM' },
  { color: 'fd-orange', text: 'ACT-001 delayed — review pending',   time: 'Yesterday 11:00 AM' },
  { color: 'fd-yellow', text: 'ACT-004 rejected by Committee Head', time: 'Feb 20, 02:15 PM' },
  { color: 'fd-blue',   text: 'ACT-005 closed successfully',        time: 'Feb 15, 10:00 AM' },
];

// ── Helpers ──
function statusClass(s) {
  return { Created:'s-created', Review:'s-review', Approved:'s-approved', Rejected:'s-rejected', Closed:'s-closed' }[s] || 's-created';
}

function animateCount(id, target) {
  let current = 0;
  const el   = document.getElementById(id);
  const step = Math.ceil(target / 20);
  const t    = setInterval(() => {
    current = Math.min(current + step, target);
    el.textContent = current;
    if (current >= target) clearInterval(t);
  }, 40);
}

// ── Render functions ──
function renderTable() {
  document.getElementById('actionBody').innerHTML = actions.map(a => {
    const delay = a.getDelayHours();
    const dc    = delay === 0 ? 'delay-ok' : delay < 24 ? 'delay-warn' : 'delay-bad';
    return `<tr>
      <td style="color:var(--accent)">${a.id}</td>
      <td>${a.title}</td>
      <td style="color:var(--muted)">${a.committee}</td>
      <td><span class="status-badge ${statusClass(a.status)}">${a.status}</span></td>
      <td class="${dc}">${delay === 0 ? 'On time' : delay + 'h late'}</td>
    </tr>`;
  }).join('');
}

function renderFeed() {
  document.getElementById('feedList').innerHTML = feedItems.map(f =>
    `<div class="feed-item">
      <div class="feed-dot ${f.color}"></div>
      <div><div class="feed-action">${f.text}</div><div class="feed-time">${f.time}</div></div>
    </div>`
  ).join('');
}

function renderDelayBars() {
  const sorted = [...committees].sort((a,b) => a.delay - b.delay);
  const maxAbs = Math.max(...sorted.map(c => Math.abs(c.delay)), 1);
  document.getElementById('delayBars').innerHTML = sorted.map(c => {
    const pct = Math.round((Math.abs(c.delay) / maxAbs) * 100);
    const col = c.delay <= 0 ? '#eab308' : c.delay < 1 ? '#10b981' : c.delay < 2 ? '#10b981' : c.delay < 3 ? '#ef4444' : '#f97316';
    // Pick color: green for <1.5, orange for 1.5-3, red for >3, yellow for negative
    const barColor = c.delay < 0 ? '#eab308' : c.delay < 1.5 ? '#10b981' : c.delay < 3 ? '#f97316' : '#ef4444';
    // For T&P which has mixed color in the screenshot (red+orange), just use orange for high
    const finalColor = c.delay > 4 ? '#f97316' : barColor;
    return `<div class="hb-row">
      <span class="hb-name">${c.name}</span>
      <div class="hb-track"><div class="hb-fill" style="width:0%;background:${finalColor}" data-w="${pct}%"></div></div>
      <span class="hb-val" style="color:${finalColor}">${c.delay > 0 ? c.delay : c.delay}</span>
    </div>`;
  }).join('');
  setTimeout(() => {
    document.querySelectorAll('#delayBars .hb-fill').forEach(el => el.style.width = el.dataset.w);
  }, 100);
}

function renderScoreBars() {
  const sorted = [...committees].sort((a,b) => b.score - a.score);
  document.getElementById('scoreBars').innerHTML = sorted.map(c => {
    const col = c.score >= 80 ? '#10b981' : c.score >= 60 ? '#f59e0b' : '#ef4444';
    const trend = c.score >= 80 ? '↗' : c.score >= 60 ? '→' : '↘';
    return `<div class="acc-row">
      <span class="acc-name">${c.name}</span>
      <div class="acc-track"><div class="acc-fill" style="width:0%;background:${col}" data-w="${c.score}%"></div></div>
      <span class="acc-pct" style="color:${col}"><span class="acc-trend">${trend}</span> ${c.score}%</span>
    </div>`;
  }).join('');
  setTimeout(() => {
    document.querySelectorAll('#scoreBars .acc-fill').forEach(el => el.style.width = el.dataset.w);
  }, 120);
}

function updateKPIs() {
  const total    = actions.length;
  const delayed  = actions.filter(a => a.isDelayed()).length;
  const approved = actions.filter(a => a.status === 'Approved' || a.status === 'Closed').length;
  const avgDelay = (committees.reduce((s, c) => s + c.delay, 0) / committees.length).toFixed(1);

  animateCount('kTotal',   total);
  animateCount('kDelayed', delayed);
  animateCount('kApproved',approved);
  document.getElementById('kAvgDelay').textContent = avgDelay + 'd';
}

function loadUser() {
  const u = User.fromSession();
  if (!u) {
    // Not authenticated — redirect to login
    window.location.href = '../index.html';
    return;
  }
  const initials = u.name ? u.name.split(' ').map(w => w[0]).join('').substring(0, 2).toUpperCase() : u.id.substring(0, 2).toUpperCase();
  document.getElementById('avatarInitial').textContent = initials;
  document.getElementById('sidebarName').textContent   = u.name || u.id;
  document.getElementById('sidebarRole').textContent   = u.role;
}

function updateClock() {
  document.getElementById('liveClock').textContent = new Date().toLocaleTimeString();
}

// ── Audit Log Data (Head-only) ──
const auditLogs = [
  { name:'Dr. Joshi',   role:'Head',    initials:'DJ', desc:'Approved action after review',           time:'2026-02-28 14:32:10', actionId:'ACT-005', session:'sess_a8f3' },
  { name:'Dr. Sharma',  role:'Member',  initials:'DS', desc:'Created purchase request',               time:'2026-02-20 09:15:44', actionId:'ACT-001', session:'sess_b2c1' },
  { name:'Admin',       role:'Auditor', initials:'AD', desc:'Rejected — incomplete documents',        time:'2026-02-22 16:45:00', actionId:'ACT-006', session:'sess_d4e7' },
  { name:'Dr. Verma',   role:'Head',    initials:'DV', desc:'Closed ahead of schedule',               time:'2026-02-13 11:20:33', actionId:'ACT-003', session:'sess_f1a9' },
  { name:'Mr. Gupta',   role:'Member',  initials:'MG', desc:'Submitted maintenance fund request',     time:'2026-03-01 08:00:12', actionId:'ACT-004', session:'sess_c3b5' },
  { name:'Prof. Mehta', role:'Member',  initials:'PM', desc:'Uploaded budget spreadsheet',             time:'2026-02-25 10:05:28', actionId:'ACT-002', session:'sess_e6d2' },
];

function renderAuditLog() {
  const u = User.fromSession();
  if (!u || u.role.toLowerCase() !== 'head') return;
  document.getElementById('auditLogPanel').classList.add('visible');

  const roleClass = { Head:'av-head', Member:'av-member', Auditor:'av-auditor' };
  const roleBadge = { Head:'r-head', Member:'r-member', Auditor:'r-auditor' };

  document.getElementById('auditLogList').innerHTML = auditLogs.map(l => {
    const dt = new Date(l.time.replace(' ','T'));
    const fmtDate = dt.toLocaleDateString('en-IN', { year:'numeric', month:'short', day:'2-digit' });
    const fmtTime = dt.toLocaleTimeString('en-IN', { hour:'2-digit', minute:'2-digit', second:'2-digit', hour12:true });
    return `<div class="al-item">
      <div class="al-avatar ${roleClass[l.role] || 'av-member'}">${l.initials}</div>
      <div class="al-body">
        <div><span class="al-name">${l.name}</span><span class="al-role ${roleBadge[l.role] || 'r-member'}">${l.role === 'Head' ? 'Committee Head' : l.role}</span></div>
        <div class="al-desc">${l.desc}</div>
        <div class="al-meta">
          <span class="al-time">${fmtDate} ${fmtTime}</span>
          <span><span class="al-dot"></span></span>
          <span class="al-action-id">${l.actionId}</span>
          <span><span class="al-dot"></span></span>
          <span class="al-sess">${l.session}</span>
        </div>
      </div>
    </div>`;
  }).join('');
}

// ── Notification System ──
const notifications = [
  { id:1, icon:'⚠️', cls:'ni-warn', text:'ACT-001 is delayed — review pending for 3 days', time:'10 min ago', unread:true },
  { id:2, icon:'✅', cls:'ni-ok',   text:'ACT-002 approved by Dr. Sharma',                 time:'1 hour ago', unread:true },
  { id:3, icon:'ℹ️', cls:'ni-info', text:'New action ACT-003 assigned to your committee',   time:'2 hours ago', unread:true },
  { id:4, icon:'❌', cls:'ni-err',  text:'ACT-004 rejected — incomplete documents',         time:'Yesterday',  unread:false },
  { id:5, icon:'✅', cls:'ni-ok',   text:'ACT-005 closed ahead of schedule',                time:'Yesterday',  unread:false },
  { id:6, icon:'ℹ️', cls:'ni-info', text:'Monthly audit report is ready for download',      time:'2 days ago', unread:false },
  { id:7, icon:'⚠️', cls:'ni-warn', text:'NAAC committee deadline approaching in 2 days',   time:'2 days ago', unread:false },
];

function initNotifications() {
  const btn      = document.getElementById('notifBtn');
  const dropdown = document.getElementById('notifDropdown');
  const dot      = document.getElementById('notifDot');
  const countEl  = document.getElementById('notifCount');
  const listEl   = document.getElementById('notifList');
  const markAll  = document.getElementById('markAllRead');

  if (!btn || !dropdown) return;

  function renderNotifList() {
    const unreadCount = notifications.filter(n => n.unread).length;
    countEl.textContent = unreadCount;
    dot.style.display = unreadCount > 0 ? 'block' : 'none';

    if (notifications.length === 0) {
      listEl.innerHTML = '<div class="notif-empty">No notifications yet</div>';
      return;
    }

    listEl.innerHTML = notifications.map(n => `
      <div class="notif-item ${n.unread ? 'unread' : ''}" data-nid="${n.id}">
        <div class="notif-icon-wrap ${n.cls}">${n.icon}</div>
        <div class="notif-body">
          <div class="notif-text">${n.text}</div>
          <div class="notif-time">${n.time}</div>
        </div>
        ${n.unread ? '<div class="notif-unread-dot"></div>' : ''}
      </div>
    `).join('');

    // Click individual notification to mark as read
    listEl.querySelectorAll('.notif-item.unread').forEach(el => {
      el.addEventListener('click', () => {
        const nid = parseInt(el.dataset.nid);
        const notif = notifications.find(n => n.id === nid);
        if (notif) { notif.unread = false; renderNotifList(); }
      });
    });
  }

  // Toggle dropdown
  btn.addEventListener('click', (e) => {
    e.stopPropagation();
    dropdown.classList.toggle('open');
  });

  // Close on outside click
  document.addEventListener('click', (e) => {
    if (!dropdown.contains(e.target) && e.target !== btn) {
      dropdown.classList.remove('open');
    }
  });

  // Mark all as read
  markAll.addEventListener('click', () => {
    notifications.forEach(n => n.unread = false);
    renderNotifList();
  });

  renderNotifList();
}

// ── Init ──
loadUser();
renderTable();
renderFeed();
renderDelayBars();
renderScoreBars();
updateKPIs();
renderAuditLog();
initNotifications();
setInterval(updateClock, 1000);
updateClock();