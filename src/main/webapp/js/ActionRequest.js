/**
 * ActionRequest.js
 * JS Class for a new action request submitted via the form.
 * Covers: JS Class practical + JSON conversion
 * Member 1 owns this file.
 */

class ActionRequest {
  constructor(title, type, committee, dueDate, description, priority, cost, approver) {
    this.id          = 'ACT-' + Date.now();
    this.title       = title;
    this.type        = type;
    this.committee   = committee;
    this.dueDate     = dueDate;
    this.description = description;
    this.priority    = priority;
    this.cost        = cost;
    this.approver    = approver;
    this.status      = 'Created';
    this.createdAt   = new Date().toISOString();
  }

  toJSON() {
    return JSON.stringify(this);
  }

  // Submits to Java Servlet via fetch (Member 2 wires the backend)
  async submitToServlet() {
    const formData = new FormData();
    Object.entries(this).forEach(([key, val]) => formData.append(key, val));

    // Member 2: ensure ActionServlet is mapped at /ActionServlet
    const response = await fetch('../ActionServlet', {
      method: 'POST',
      body: formData
    });
    return response;
  }
}