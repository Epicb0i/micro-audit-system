/**
 * User.js
 * JS Class representing a logged-in user.
 * Covers: JS Class practical + JSON conversion
 * Member 1 owns this file.
 */

class User {
  constructor(id, role) {
    this.id = id;
    this.role = role;
    this.name = id;
    this.department = '';
    this.email = '';
    this.phone = '';
    this.committee = '';
    this.loginTime = new Date().toISOString();
    this.sessionId = 'SES_' + Math.random().toString(36).substr(2, 9).toUpperCase();
  }

  toJSON() {
    return JSON.stringify({
      id: this.id,
      role: this.role,
      name: this.name,
      department: this.department,
      email: this.email,
      phone: this.phone,
      committee: this.committee,
      loginTime: this.loginTime,
      sessionId: this.sessionId
    });
  }

  static fromSession() {
    const raw = sessionStorage.getItem('currentUser');
    if (!raw) return null;
    const data = JSON.parse(raw);
    const u = new User(data.id, data.role);
    u.name = data.name || data.id;
    u.department = data.department || '';
    u.email = data.email || '';
    u.phone = data.phone || '';
    u.committee = data.committee || '';
    u.loginTime = data.loginTime;
    u.sessionId = data.sessionId;
    return u;
  }
}