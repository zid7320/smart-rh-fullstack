import { Injectable, NgZone, inject } from '@angular/core';

// ─────────────────────────────────────────────────────────────────────────────
// SMART RH — Voice service
// Wraps the browser Web Speech API: SpeechRecognition (speech → text) and
// speechSynthesis (text → speech). Supports French (fr-FR) and English (en-US).
// Best support: Chrome / Edge. (Firefox/Safari have partial recognition.)
// ─────────────────────────────────────────────────────────────────────────────

export type VoiceLang = 'fr' | 'en';

export interface RecognitionCallbacks {
  onInterim?: (text: string) => void;
  onFinal?: (text: string) => void;
  onStart?: () => void;
  onEnd?: () => void;
  onError?: (err: string) => void;
}

@Injectable({ providedIn: 'root' })
export class VoiceService {
  private zone = inject(NgZone);

  private recognition: any = null;
  private voices: SpeechSynthesisVoice[] = [];
  private speakingQueue = 0;
  onSpeakingEnd: (() => void) | null = null;

  readonly supportsRecognition =
    !!((window as any).SpeechRecognition || (window as any).webkitSpeechRecognition);
  readonly supportsSynthesis = 'speechSynthesis' in window;

  constructor() {
    if (this.supportsSynthesis) {
      this.loadVoices();
      window.speechSynthesis.onvoiceschanged = () => this.loadVoices();
    }
  }

  private loadVoices(): void {
    this.voices = window.speechSynthesis.getVoices();
  }

  /** EN/FR voices available for the picker. */
  listVoices(): SpeechSynthesisVoice[] {
    return this.voices.filter((v) => /^(en|fr)/i.test(v.lang));
  }

  // ── Recognition (STT) ──────────────────────────────────────────────────────
  startListening(lang: VoiceLang, cb: RecognitionCallbacks): void {
    const SR = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
    if (!SR) {
      cb.onError?.('unsupported');
      return;
    }
    this.stopSpeaking(); // barge-in: stop talking when the user speaks

    const rec = new SR();
    rec.lang = lang === 'fr' ? 'fr-FR' : 'en-US';
    rec.interimResults = true;
    rec.continuous = false;
    rec.maxAlternatives = 1;

    let finalText = '';

    rec.onstart = () => this.zone.run(() => cb.onStart?.());
    rec.onresult = (e: any) => {
      let interim = '';
      for (let i = e.resultIndex; i < e.results.length; i++) {
        const t = e.results[i][0].transcript;
        if (e.results[i].isFinal) finalText += t;
        else interim += t;
      }
      this.zone.run(() => cb.onInterim?.((finalText + interim).trim()));
    };
    rec.onerror = (e: any) =>
      this.zone.run(() => {
        if (e.error !== 'no-speech' && e.error !== 'aborted') cb.onError?.(e.error);
      });
    rec.onend = () =>
      this.zone.run(() => {
        const text = finalText.trim();
        if (text) cb.onFinal?.(text);
        cb.onEnd?.();
      });

    this.recognition = rec;
    try {
      rec.start();
    } catch {
      /* already started */
    }
  }

  stopListening(): void {
    try {
      this.recognition?.stop();
    } catch {
      /* noop */
    }
  }

  // ── Synthesis (TTS) ──────────────────────────────────────────────────────
  speak(text: string, lang: VoiceLang, voiceURI: string, rate = 1, pitch = 1): void {
    text = text.trim();
    if (!text || !this.supportsSynthesis) return;

    const u = new SpeechSynthesisUtterance(text);
    const chosen =
      this.voices.find((v) => v.voiceURI === voiceURI) ||
      this.voices.find((v) => v.lang.toLowerCase().startsWith(lang));
    if (chosen) u.voice = chosen;
    u.lang = (chosen && chosen.lang) || (lang === 'fr' ? 'fr-FR' : 'en-US');
    u.rate = rate;
    u.pitch = pitch;

    this.speakingQueue++;
    const done = () =>
      this.zone.run(() => {
        this.speakingQueue = Math.max(0, this.speakingQueue - 1);
        if (this.speakingQueue === 0) this.onSpeakingEnd?.();
      });
    u.onend = done;
    u.onerror = done;
    window.speechSynthesis.speak(u);
  }

  isSpeaking(): boolean {
    return this.supportsSynthesis && window.speechSynthesis.speaking;
  }

  stopSpeaking(): void {
    if (this.supportsSynthesis) window.speechSynthesis.cancel();
    this.speakingQueue = 0;
  }
}
