/**
 * login.js
 * Handles login form interactions and session creation.
 * Member 1 owns this file.
 */

function selectRole(btn) {
  document.querySelectorAll('.role-btn').forEach(b => b.classList.remove('active'));
  btn.classList.add('active');
  document.getElementById('selectedRole').value = btn.textContent.trim();
}

function handleLogin() {
  const id   = document.getElementById('userId').value.trim();
  const pw   = document.getElementById('password').value.trim();
  const role = document.getElementById('selectedRole').value;
  const err  = document.getElementById('errMsg');
  const btn  = document.querySelector('.login-btn');

  if (!id || !pw) {
    err.style.display = 'block';
    err.textContent = '⚠ Please fill in all fields.';
    return;
  }

  err.style.display = 'none';
  btn.textContent   = 'Authenticating...';
  btn.disabled      = true;

  // Attempt real servlet authentication first; fallback to demo mode
  fetch('api/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({ userId: id, password: pw, role: role })
  })
  .then(res => res.json().then(data => ({ ok: res.ok, data })))
  .then(({ ok, data }) => {
    if (ok && data.success) {
      // Store server-authenticated user in sessionStorage
      const user = new User(data.staffId || id, data.role || role);
      user.name = data.name || id;
      user.sessionId = data.sessionId || user.sessionId;
      sessionStorage.setItem('currentUser', user.toJSON());
      window.location.href = 'pages/dashboard.html';
    } else {
      showError(data.message || 'Invalid credentials. Please try again.');
    }
  })
  .catch(() => {
    // Fallback: demo mode when servlet is unavailable (static file serving)
    console.warn('Servlet unavailable — using demo login mode');
    const demoUsers = {
      'STF001': { name: 'Dr. Sharma',   role: 'Head' },
      'STF002': { name: 'Prof. Patel',  role: 'Member' },
      'STF003': { name: 'Dr. Kumar',    role: 'Member' },
      'STF004': { name: 'Prof. Singh',  role: 'Auditor' },
      'STF005': { name: 'Dr. Mehta',    role: 'Head' },
      'STF006': { name: 'Prof. Gupta',  role: 'Member' }
    };
    const demo = demoUsers[id.toUpperCase()];
    if (demo && pw === 'password123') {
      const user = new User(id.toUpperCase(), demo.role);
      user.name = demo.name;
      user.department = demo.department || 'Computer Engineering';
      user.email = demo.email || '';
      user.phone = demo.phone || '';
      user.committee = demo.committee || '';
      sessionStorage.setItem('currentUser', user.toJSON());
      window.location.href = 'pages/dashboard.html';
    } else {
      // Check locally registered users (from signup page)
      const registered = JSON.parse(localStorage.getItem('registeredUsers') || '[]');
      const regUser = registered.find(u => u.staffId === id.toUpperCase() && u.password === pw);
      if (regUser) {
        const user = new User(regUser.staffId, regUser.role);
        user.name = regUser.name;
        user.department = regUser.department || '';
        user.email = regUser.email || '';
        user.phone = regUser.phone || '';
        user.committee = regUser.committee || '';
        sessionStorage.setItem('currentUser', user.toJSON());
        window.location.href = 'pages/dashboard.html';
      } else {
        showError('Invalid credentials. Check your Staff ID and password.');
      }
    }
  });
}

function showError(msg) {
  const err = document.getElementById('errMsg');
  const btn = document.querySelector('.login-btn');
  err.style.display = 'block';
  err.textContent   = '⚠ ' + msg;
  btn.textContent   = 'Sign In';
  btn.disabled      = false;
}

// Enter key triggers login
document.addEventListener('keydown', function(e) {
  if (e.key === 'Enter') { e.preventDefault(); handleLogin(); }
});