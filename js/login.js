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

  // Create User object and store as JSON in sessionStorage
  // This mirrors Java HttpSession.setAttribute("user", user)
  const user = new User(id, role);
  sessionStorage.setItem('currentUser', user.toJSON());

  /*
   * MEMBER 2 — replace the setTimeout below with a real fetch to LoginServlet:
   *
   * fetch('LoginServlet', {
   *   method: 'POST',
   *   body: new URLSearchParams({ userId: id, password: pw, role: role })
   * })
   * .then(res => res.ok ? window.location.href = 'pages/dashboard.html' : showError())
   * .catch(() => showError());
   */
  setTimeout(() => {
    window.location.href = 'pages/dashboard.html';
  }, 1000);
}