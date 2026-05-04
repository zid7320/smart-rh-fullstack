// ─────────────────────────────────────────────────────────────────────────────
// SMART RH 4.0 — Development environment
//
// Backend: http://localhost:8081  (Docker port mapping: 8081 -> 8080 internal)
// Frontend: http://localhost:4200 (Angular dev server)
//
// SockJS endpoint MUST be HTTP (not ws://)
//   SockJS automatically tries WebSocket first, then falls back to HTTP polling
//   if WebSocket fails. The endpoint handles both protocols.
// ─────────────────────────────────────────────────────────────────────────────
export const environment = {
  production: false,

  /** REST API base — no trailing slash */
  apiBaseUrl: 'http://localhost:8081',

  /** SockJS endpoint — HTTP (SockJS handles protocol upgrade) */
  wsBaseUrl: 'http://localhost:8081/ws',

  /** STOMP topic for real-time attendance events */
  stompTopic: '/topic/attendance',

  /** STOMP app-destination prefix (reserved for @MessageMapping) */
  stompAppPrefix: '/app',

  /** Optional Google Calendar integration (public calendar) */
  googleCalendarApiKey: '',
  googleCalendarId: ''
};
