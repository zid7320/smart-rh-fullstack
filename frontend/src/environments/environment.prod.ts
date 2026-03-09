// ─────────────────────────────────────────────────────────────────────────────
// SMART RH 4.0 — Production / Docker environment
//
// docker-compose.yml: backend ports "8081:8080"
// Change apiBaseUrl when deploying behind a reverse proxy.
// ─────────────────────────────────────────────────────────────────────────────
export const environment = {
  production: true,
  apiBaseUrl: 'http://localhost:8081',
  wsBaseUrl:  'http://localhost:8081/ws',
  stompTopic: '/topic/attendance',
  stompAppPrefix: '/app'
};
