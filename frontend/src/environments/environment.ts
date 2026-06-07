// ─────────────────────────────────────────────────────────────────────────────
// SMART RH 4.0 — Development environment
//
// Backend: http://localhost:8081  (Docker port mapping: 8081 -> 8080 internal)
// Frontend: http://localhost:55167 (or any available port)
//
// SockJS endpoint MUST be HTTP — NOT ws://
//   Source: WebSocketConfig.java → registry.addEndpoint("/ws").withSockJS()
//   Javadoc example: new SockJS('http://localhost:8081/ws')
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
  stompAppPrefix: '/app',

  /** Optional Google Calendar integration (public calendar) */
  googleCalendarApiKey: '',
  googleCalendarId: '',

  /** Groq API key — used by Lyra (the AI assistant). Leave '' and set it
   *  in-app via the key button, OR paste your REGENERATED key here.
   *  WARNING: a key placed here ships to the browser. For production,
   *  proxy through the Spring backend instead. */
  groqApiKey: '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',

  /** Groq model for the assistant (agentic tools model from the playground) */
  groqModel: 'groq/compound-mini'
};
