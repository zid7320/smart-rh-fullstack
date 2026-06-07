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
  stompAppPrefix: '/app',
  googleCalendarApiKey: '',
  googleCalendarId: '',

  /** Groq API key — used by Lyra (the AI assistant). Leave '' and set it
   *  in-app via the key button, OR paste your REGENERATED key here.
   *  WARNING: a key placed here ships to the browser. For production,
   *  proxy through the Spring backend instead. */
  groqApiKey: '',

  /** Groq model for the assistant (agentic tools model from the playground) */
  groqModel: 'groq/compound-mini'
};
