/**
 * Action.js
 * JS Class for a committee action — mirrors the Java Action entity.
 * Covers: JS Class practical + JSON conversion + delay computation
 * Member 1 owns this file.
 */

class Action {
  constructor(id, title, committee, status, createdAt, expectedClose) {
    this.id = id;
    this.title = title;
    this.committee = committee;
    this.status = status;
    this.createdAt = new Date(createdAt);
    this.expectedClose = new Date(expectedClose);
  }

  // Mirrors Java: action.getDelay() method
  getDelayHours() {
    if (this.status === 'Approved' || this.status === 'Closed') return 0;
    const now = new Date();
    const diff = now - this.expectedClose;
    return Math.max(0, Math.floor(diff / 3600000));
  }

  isDelayed() {
    return this.getDelayHours() > 0;
  }

  toJSON() {
    return JSON.stringify({
      id: this.id,
      title: this.title,
      committee: this.committee,
      status: this.status,
      createdAt: this.createdAt,
      expectedClose: this.expectedClose,
      delayHours: this.getDelayHours()
    });
  }
}