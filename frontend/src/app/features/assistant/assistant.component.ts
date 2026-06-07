import {
  Component,
  inject,
  signal,
  computed,
  ElementRef,
  ViewChild,
  AfterViewChecked,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthService } from '../../core/services/auth.service';
import { GroqService, ChatMessage } from '../../core/api/groq.service';
import { VoiceService, VoiceLang } from '../../core/api/voice.service';

interface UiMessage {
  role: 'user' | 'assistant';
  text: string;
}

type StageState = 'idle' | 'listening' | 'thinking' | 'speaking';

// Routes Lyra is allowed to open by voice, with FR/EN aliases for the prompt.
const NAV_MAP: Record<string, string> = {
  '/dashboard': 'Tableau de bord / Dashboard',
  '/employees': 'Employés / Employees',
  '/posts': 'Postes / Positions',
  '/competences': 'Compétences / Skills',
  '/recruitments': 'Recrutements / Recruitment',
  '/leaves': 'Congés / Leaves',
  '/payroll': 'Paie / Payroll',
  '/evaluations': 'Évaluations / Evaluations',
  '/trainings': 'Formations / Trainings',
  '/attendance': 'Présences / Attendance',
  '/bureau': 'Capteurs Bureau / Office Sensors',
  '/bi': 'Reporting BI / BI',
  '/planning': 'Planning',
  '/contracts': 'Contrats / Contracts',
  '/dossiers-rh': 'Dossiers RH / HR Files',
  '/candidats': 'Candidats / Candidates',
  '/profile': 'Mon Profil / My Profile',
};

@Component({
  selector: 'app-assistant',
  standalone: true,
  imports: [FormsModule, MatIconModule, MatButtonModule, MatTooltipModule],
  templateUrl: './assistant.component.html',
  styleUrl: './assistant.component.scss',
})
export class AssistantComponent implements AfterViewChecked {
  private auth = inject(AuthService);
  private groq = inject(GroqService);
  private voice = inject(VoiceService);
  private router = inject(Router);

  @ViewChild('feed') feedRef?: ElementRef<HTMLDivElement>;

  // ── UI state (signals) ─────────────────────────────────────────────────
  readonly open = signal(false);
  readonly stage = signal<StageState>('idle');
  readonly listening = signal(false);
  readonly busy = signal(false);
  readonly lang = signal<VoiceLang>('fr');
  readonly muted = signal(false);
  readonly conversationMode = signal(false);
  readonly showSettings = signal(false);
  readonly messages = signal<UiMessage[]>([]);
  readonly draft = signal('');
  readonly apiKeyInput = signal('');
  readonly hasKey = signal(this.groq.hasKey());

  readonly charName = 'Lyra';
  readonly supportsVoice = this.voice.supportsRecognition;

  readonly statusText = computed(() => {
    const fr = this.lang() === 'fr';
    switch (this.stage()) {
      case 'listening': return fr ? 'Je vous écoute…' : "I'm listening…";
      case 'thinking':  return fr ? 'Je réfléchis…'   : 'Thinking…';
      case 'speaking':  return fr ? 'Lyra parle…'      : 'Lyra is speaking…';
      default:          return fr ? 'Prêt·e à vous aider' : 'Ready to help';
    }
  });

  // Model conversation history (separate from UI list so we can prepend system)
  private history: ChatMessage[] = [];
  private abort?: AbortController;
  private shouldScroll = false;

  constructor() {
    this.voice.onSpeakingEnd = () => {
      if (this.busy()) return;
      this.stage.set('idle');
      if (this.conversationMode() && !this.listening()) {
        setTimeout(() => this.startVoice(), 350); // hands-free loop
      }
    };
  }

  ngAfterViewChecked(): void {
    if (this.shouldScroll && this.feedRef) {
      this.feedRef.nativeElement.scrollTop = this.feedRef.nativeElement.scrollHeight;
      this.shouldScroll = false;
    }
  }

  // ── Open / close ─────────────────────────────────────────────────────────
  toggle(): void {
    this.open.update((v) => !v);
    if (this.open() && this.messages().length === 0) this.greet();
  }

  close(): void {
    this.open.set(false);
    this.voice.stopSpeaking();
    this.voice.stopListening();
  }

  private greet(): void {
    const user = this.auth.currentUser();
    const name = user?.username ?? '';
    const fr = this.lang() === 'fr';
    const hello = fr
      ? `Bonjour ${name} ✦ Je suis Lyra, votre assistante Smart RH. Demandez-moi d'ouvrir un module, d'expliquer une fonctionnalité, ou parlez-moi simplement — au micro ou par écrit.`
      : `Hi ${name} ✦ I'm Lyra, your Smart RH assistant. Ask me to open a module, explain a feature, or just talk to me — by mic or text.`;
    this.pushMessage('assistant', hello);
  }

  // ── Language / controls ────────────────────────────────────────────────
  setLang(l: VoiceLang): void {
    this.lang.set(l);
  }

  toggleMute(): void {
    this.muted.update((v) => !v);
    if (this.muted()) this.voice.stopSpeaking();
  }

  toggleConversation(): void {
    this.conversationMode.update((v) => !v);
    if (this.conversationMode() && !this.listening() && !this.busy() && !this.voice.isSpeaking()) {
      this.startVoice();
    }
  }

  clearChat(): void {
    this.history = [];
    this.messages.set([]);
    this.voice.stopSpeaking();
    this.stage.set('idle');
    this.greet();
  }

  // ── Settings (API key) ────────────────────────────────────────────────
  openSettings(): void {
    this.apiKeyInput.set(this.groq.getApiKey());
    this.showSettings.set(true);
  }

  saveKey(): void {
    this.groq.setApiKey(this.apiKeyInput());
    this.hasKey.set(this.groq.hasKey());
    this.showSettings.set(false);
  }

  // ── Voice ─────────────────────────────────────────────────────────────
  micClick(): void {
    if (this.listening()) {
      this.voice.stopListening();
    } else {
      this.startVoice();
    }
  }

  private startVoice(): void {
    if (this.busy() || !this.supportsVoice) return;
    this.voice.startListening(this.lang(), {
      onStart: () => {
        this.listening.set(true);
        this.stage.set('listening');
      },
      onInterim: (t) => this.draft.set(t),
      onFinal: (t) => {
        this.draft.set('');
        this.send(t);
      },
      onEnd: () => {
        this.listening.set(false);
        if (this.stage() === 'listening') this.stage.set('idle');
      },
      onError: () => {
        this.listening.set(false);
        this.stage.set('idle');
      },
    });
  }

  // ── Sending / streaming ────────────────────────────────────────────────
  sendDraft(): void {
    const t = this.draft().trim();
    if (!t) return;
    this.draft.set('');
    this.send(t);
  }

  async send(text: string): Promise<void> {
    if (this.busy()) return;
    if (!this.groq.hasKey()) {
      this.openSettings();
      return;
    }

    this.pushMessage('user', text);
    this.history.push({ role: 'user', content: text });
    this.busy.set(true);
    this.stage.set('thinking');

    const sys = this.buildSystemPrompt();
    const idx = this.pushMessage('assistant', '');
    let spoken = 0;
    let started = false;

    this.abort = new AbortController();
    // Live streaming with incremental speech:
    try {
      let accumulated = '';
      await this.groq.streamChat(
        [{ role: 'system', content: sys }, ...this.history.slice(-18)],
        {
          temperature: 0.9,
          signal: this.abort.signal,
          onToken: (delta) => {
            if (!started) {
              started = true;
              this.stage.set('speaking');
            }
            accumulated += delta;
            this.updateMessage(idx, this.stripTokens(accumulated));
            spoken = this.speakIncremental(accumulated, spoken);
          },
        }
      );

      const rest = accumulated.slice(spoken);
      if (rest.trim()) this.speak(this.stripTokens(rest));

      const clean = this.stripTokens(accumulated);
      this.updateMessage(idx, clean || (this.lang() === 'fr' ? '(silence)' : '(no reply)'));
      this.history.push({ role: 'assistant', content: clean || '…' });
      this.handleNavigation(accumulated);
    } catch (err: any) {
      const msg = err?.message === 'NO_KEY'
        ? (this.lang() === 'fr' ? 'Clé API manquante.' : 'API key missing.')
        : `⚠ ${err?.message ?? 'error'}`;
      this.updateMessage(idx, msg);
      this.stage.set('idle');
    } finally {
      this.busy.set(false);
      if (!this.voice.isSpeaking()) {
        this.stage.set('idle');
        if (this.conversationMode() && !this.listening()) {
          setTimeout(() => this.startVoice(), 350);
        }
      }
    }
  }

  stopGeneration(): void {
    this.abort?.abort();
    this.voice.stopSpeaking();
    this.busy.set(false);
    this.stage.set('idle');
  }

  // ── Speech chunking: speak each completed sentence as it arrives ─────────
  private speakIncremental(text: string, from: number): number {
    if (this.muted()) return text.length;
    const slice = this.stripTokens(text.slice(from));
    const matches = [...slice.matchAll(/[^.!?…\n]+[.!?…\n]+/g)];
    if (!matches.length) return from;
    let consumed = from;
    for (const m of matches) {
      this.speak(m[0]);
      consumed = from + (m.index ?? 0) + m[0].length;
    }
    return consumed;
  }

  private speak(text: string): void {
    if (this.muted()) return;
    this.voice.speak(text, this.lang(), '', 1.02, 1.05);
  }

  // ── Navigation tokens: [[go:/route]] ────────────────────────────────────
  private stripTokens(text: string): string {
    return text.replace(/\[\[go:[^\]]*\]\]/gi, '').trim();
  }

  private handleNavigation(raw: string): void {
    const m = raw.match(/\[\[go:(\/[a-z0-9-]+)\]\]/i);
    if (m && NAV_MAP[m[1]]) {
      setTimeout(() => this.router.navigateByUrl(m[1]), 400);
    }
  }

  // ── Persona / system prompt ──────────────────────────────────────────────
  private buildSystemPrompt(): string {
    const user = this.auth.currentUser();
    const route = this.router.url;
    const navList = Object.entries(NAV_MAP)
      .map(([path, label]) => `${path} → ${label}`)
      .join('; ');

    return [
      `You are Lyra, the built-in AI assistant of "Smart RH 4.0", a Human Resources management platform (Angular + Spring Boot).`,
      `Your personality: warm, witty, sharp and genuinely helpful — like a brilliant HR colleague. You are concise and natural, never robotic, no corporate filler.`,
      `You are fluent in French and English. ALWAYS reply in the same language the user writes/speaks. Keep answers spoken-friendly: short clear sentences, avoid long bullet lists unless asked.`,
      `The current user is "${user?.username ?? 'unknown'}" with role "${user?.role ?? 'unknown'}". They are currently on the page "${route}".`,
      `The app has these modules (employees, leaves, payroll, attendance, IoT office sensors, recruitment, contracts, planning, BI reporting, etc.).`,
      `NAVIGATION: If the user clearly wants to open/go to a section, append a control token at the VERY END of your reply in the exact form [[go:/route]] using one of these routes: ${navList}. Add the token only when navigation is intended, and never mention or explain the token to the user.`,
      `You can also use web search when the user asks about something current or external.`,
    ].join('\n');
  }

  // ── Message helpers ──────────────────────────────────────────────────────
  private pushMessage(role: 'user' | 'assistant', text: string): number {
    const arr = [...this.messages(), { role, text }];
    this.messages.set(arr);
    this.shouldScroll = true;
    return arr.length - 1;
  }

  private updateMessage(idx: number, text: string): void {
    const arr = [...this.messages()];
    if (arr[idx]) {
      arr[idx] = { ...arr[idx], text };
      this.messages.set(arr);
      this.shouldScroll = true;
    }
  }
}
