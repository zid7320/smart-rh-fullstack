import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';

// ─────────────────────────────────────────────────────────────────────────────
// SMART RH — Groq client (browser → Groq OpenAI-compatible endpoint)
//
// Streams completions from the `groq/compound-mini` agentic model with the
// web_search / code_interpreter / visit_website tools enabled.
//
// ⚠ SECURITY: this calls Groq directly from the browser, so the API key is
// visible to anyone using the app. Fine for a demo / academic project. For a
// real deployment, proxy this through the Spring backend (POST /api/assistant)
// and keep the key server-side — the component code would not change, only
// GROQ_URL + the Authorization header.
// ─────────────────────────────────────────────────────────────────────────────

export interface ChatMessage {
  role: 'system' | 'user' | 'assistant';
  content: string;
}

export interface StreamOptions {
  temperature?: number;
  maxTokens?: number;
  signal?: AbortSignal;
  /** called with every text delta as it streams in */
  onToken?: (delta: string) => void;
}

const GROQ_URL = 'https://api.groq.com/openai/v1/chat/completions';
const KEY_OVERRIDE = 'smart_rh_groq_key'; // optional in-app override (localStorage)

@Injectable({ providedIn: 'root' })
export class GroqService {

  /** Resolve the API key: in-app override first, then environment. */
  getApiKey(): string {
    return (localStorage.getItem(KEY_OVERRIDE) || environment.groqApiKey || '').trim();
  }

  setApiKey(key: string): void {
    localStorage.setItem(KEY_OVERRIDE, key.trim());
  }

  hasKey(): boolean {
    return this.getApiKey().length > 0;
  }

  /**
   * Stream a chat completion. Resolves with the full assistant text once the
   * stream ends. Calls opts.onToken for each delta so the UI can render live.
   */
  async streamChat(messages: ChatMessage[], opts: StreamOptions = {}): Promise<string> {
    const key = this.getApiKey();
    if (!key) throw new Error('NO_KEY');

    const res = await fetch(GROQ_URL, {
      method: 'POST',
      signal: opts.signal,
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${key}`,
      },
      body: JSON.stringify({
        model: environment.groqModel || 'groq/compound-mini',
        messages,
        temperature: opts.temperature ?? 0.9,
        max_completion_tokens: opts.maxTokens ?? 1024,
        top_p: 1,
        stream: true,
        stop: null,
        compound_custom: {
          tools: { enabled_tools: ['web_search', 'code_interpreter', 'visit_website'] },
        },
      }),
    });

    if (!res.ok || !res.body) {
      const txt = await res.text().catch(() => '');
      throw new Error(`${res.status} ${txt.slice(0, 200)}`);
    }

    const reader = res.body.getReader();
    const decoder = new TextDecoder();
    let buffer = '';
    let full = '';

    // eslint-disable-next-line no-constant-condition
    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      buffer += decoder.decode(value, { stream: true });

      const lines = buffer.split('\n');
      buffer = lines.pop() ?? '';

      for (const raw of lines) {
        const line = raw.trim();
        if (!line || !line.startsWith('data:')) continue;
        const data = line.slice(5).trim();
        if (data === '[DONE]') continue;
        try {
          const json = JSON.parse(data);
          const delta: string | undefined = json?.choices?.[0]?.delta?.content;
          if (delta) {
            full += delta;
            opts.onToken?.(delta);
          }
        } catch {
          /* ignore keep-alive / partial frames */
        }
      }
    }
    return full;
  }
}
