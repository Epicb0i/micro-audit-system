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
  { name: 'Exam Committee',     delay: 12 },
  { name: 'Purchase Committee', delay: 34 },
  { name: 'Cultural Committee', delay: 8  },
  { name: 'NAAC Committee',     delay: 56 },
  { name: 'Hostel Committee',   delay: 21 },
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
  const max = Math.max(...committees.map(c => c.delay));
  document.getElementById('delayBars').innerHTML = committees.map(c => {
    const pct = Math.round((c.delay / max) * 100);
    const col = c.delay < 15 ? 'var(--accent3)' : c.delay < 35 ? 'var(--warn)' : 'var(--accent2)';
    return `<div>
      <div class="dbar-header">
        <span class="dbar-label">${c.name}</span>
        <span class="dbar-pct" style="color:${col}">${c.delay}h avg delay</span>
      </div>
      <div class="dbar-track">
        <div class="dbar-fill" style="width:0%;background:${col}" data-w="${pct}%"></div>
      </div>
    </div>`;
  }).join('');
  setTimeout(() => {
    document.querySelectorAll('.dbar-fill').forEach(el => el.style.width = el.dataset.w);
  }, 100);
}

function updateKPIs() {
  const total    = actions.length;
  const delayed  = actions.filter(a => a.isDelayed()).length;
  const approved = actions.filter(a => a.status === 'Approved' || a.status === 'Closed').length;
  const avgDelay = Math.round(committees.reduce((s, c) => s + c.delay, 0) / committees.length);

  animateCount('kTotal',   total);
  animateCount('kDelayed', delayed);
  animateCount('kApproved',approved);
  document.getElementById('kAvgDelay').textContent = avgDelay + 'h';
}

function loadUser() {
  const u = User.fromSession();
  if (u) {
    document.getElementById('avatarInitial').textContent = u.id.substring(0, 2).toUpperCase();
    document.getElementById('sidebarName').textContent   = u.id;
    document.getElementById('sidebarRole').textContent   = u.role;
  }
}

function updateClock() {
  document.getElementById('liveClock').textContent = new Date().toLocaleTimeString();
}

// ── Init ──
loadUser();
renderTable();
renderFeed();
renderDelayBars();
updateKPIs();
setInterval(updateClock, 1000);
updateClock();