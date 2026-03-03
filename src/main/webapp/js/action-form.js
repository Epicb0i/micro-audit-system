/**
 * action-form.js
 * Handles form validation, priority selection, and submission.
 * Member 1 owns this file.
 */

let selectedPriority = 'low';

function setPriority(btn, level) {
  document.querySelectorAll('.pri-btn').forEach(b => b.classList.remove('active'));
  btn.classList.add('active');
  selectedPriority = level;
  document.getElementById('priorityInput').value = level;
}

function showFileName(input) {
  const el = document.getElementById('fileName');
  if (input.files.length) {
    el.style.display = 'block';
    el.textContent   = '📎 ' + input.files[0].name;
  }
}

function validateForm() {
  const fields = [
    { id: 'actTitle',     errId: 'errTitle' },
    { id: 'actType',      errId: 'errType' },
    { id: 'actCommittee', errId: 'errCommittee' },
    { id: 'actDueDate',   errId: 'errDate' },
  ];
  let valid = true;
  fields.forEach(f => {
    const el  = document.getElementById(f.id);
    const err = document.getElementById(f.errId);
    if (!el.value.trim()) {
      el.classList.add('input-err');
      err.style.display = 'block';
      valid = false;
    } else {
      el.classList.remove('input-err');
      err.style.display = 'none';
    }
  });
  return valid;
}

function submitAction() {
  if (!validateForm()) return;

  const action = new ActionRequest(
    document.getElementById('actTitle').value,
    document.getElementById('actType').value,
    document.getElementById('actCommittee').value,
    document.getElementById('actDueDate').value,
    document.getElementById('actDesc').value,
    selectedPriority,
    document.getElementById('actCost').value,
    document.getElementById('actApprover').value
  );

  // Store in sessionStorage (demo mode)
  const existing = JSON.parse(sessionStorage.getItem('pendingActions') || '[]');
  existing.push(JSON.parse(action.toJSON()));
  sessionStorage.setItem('pendingActions', JSON.stringify(existing));

  /*
   * MEMBER 2 — uncomment to wire real servlet:
   * action.submitToServlet()
   *   .then(() => { showToast(); setTimeout(() => window.location.href = 'dashboard.html', 2000); })
   *   .catch(err => console.error(err));
   */

  showToast('✅ Action submitted successfully!', 'var(--accent3)');
  document.querySelector('.submit-btn').textContent = 'Submitted ✓';
  setTimeout(() => { window.location.href = 'dashboard.html'; }, 2000);
}

function saveDraft() {
  showToast('📝 Draft saved locally.', 'var(--warn)');
}

function showToast(msg, color) {
  const toast = document.getElementById('toast');
  toast.textContent   = msg;
  toast.style.color   = color;
  toast.style.borderColor = color;
  toast.classList.add('show');
  setTimeout(() => toast.classList.remove('show'), 2500);
}

function loadMeta() {
  const u = User.fromSession ? User.fromSession() : null;
  if (!u) {
    window.location.href = '../index.html';
    return;
  }
  const now = new Date();
  document.getElementById('subTime').textContent = now.toLocaleString();
  if (u) {
    document.getElementById('subUser').textContent    = u.name || u.id;
    document.getElementById('subRole').textContent    = u.role;
    document.getElementById('subSession').textContent = u.sessionId;
  }
  document.getElementById('actDueDate').min = now.toISOString().split('T')[0];
}

loadMeta();