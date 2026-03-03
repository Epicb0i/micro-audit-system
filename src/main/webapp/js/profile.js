/**
 * profile.js
 * Handles tab switching, heatmap, and profile save.
 * Member 1 owns this file.
 */

function switchTab(el, id) {
  document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
  document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));
  el.classList.add('active');
  document.getElementById('tab-' + id).classList.add('active');
}

function buildHeatmap() {
  const grid   = document.getElementById('heatmap');
  const colors = [
    'rgba(28,37,48,0.8)',
    'rgba(0,229,255,0.15)',
    'rgba(0,229,255,0.35)',
    'rgba(0,229,255,0.6)',
    'rgba(0,229,255,0.9)'
  ];
  for (let i = 0; i < 48; i++) {
    const cell      = document.createElement('div');
    cell.className  = 'hm-cell';
    const intensity = Math.floor(Math.random() * 5);
    cell.style.background = colors[intensity];
    cell.title      = `Activity level: ${intensity}`;
    grid.appendChild(cell);
  }
}

function saveProfile() {
  const name = document.getElementById('fullName').value;
  document.getElementById('heroName').textContent = name;

  /*
   * MEMBER 2 — uncomment to wire ProfileServlet:
   * fetch('../ProfileServlet', {
   *   method: 'POST',
   *   body: new URLSearchParams({ fullName: name, email: ..., department: ... })
   * });
   */

  const toast = document.getElementById('toast');
  toast.classList.add('show');
  setTimeout(() => toast.classList.remove('show'), 2500);
}

function loadSession() {
  const raw = sessionStorage.getItem('currentUser');
  if (raw) {
    const u = JSON.parse(raw);
    if (u.sessionId) document.getElementById('secSession').textContent = u.sessionId;
  }
}

buildHeatmap();
loadSession();