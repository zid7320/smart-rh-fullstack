// ─────────────────────────────────────────────────────────────────────────────
// SMART RH 4.0 — Development environment
//
// Backend: http://localhost:8080  (spring.profiles.active=default / H2 in-memory)
// Docker:  http://localhost:8081  → use environment.prod.ts
//
// SockJS endpoint MUST be HTTP — NOT ws://
//   Source: WebSocketConfig.java → registry.addEndpoint("/ws").withSockJS()
//   Javadoc example: new SockJS('http://localhost:8080/ws')
// ─────────────────────────────────────────────────────────────────────────────
export const environment = {
  production: false,

  /** REST API base — no trailing slash */
  apiBaseUrl: 'http://localhost:8081',

  /** SockJS endpoint — HTTP (not ws://) */
  wsBaseUrl: 'http://localhost:8081/ws',

  /** STOMP topic for real-time attendance events */
  stompTopic: '/topic/attendance',

  /** STOMP app-destination prefix (reserved for @MessageMapping) */
  stompAppPrefix: '/app'
};
