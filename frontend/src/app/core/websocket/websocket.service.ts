import { Injectable, inject, OnDestroy } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Subject } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';
import type { AttendanceRecord } from '../models/attendance.model';

@Injectable({ providedIn: 'root' })
export class WebsocketService implements OnDestroy {
  private auth   = inject(AuthService);
  private client!: Client;

  /** Stream of real-time attendance events broadcast from /topic/attendance. */
  readonly attendance$ = new Subject<AttendanceRecord>();

  get connected(): boolean {
    return this.client?.active ?? false;
  }

  connect(): void {
    if (this.client?.active) return;

    this.client = new Client({
      webSocketFactory: () => new (SockJS as any)(environment.wsBaseUrl),
      connectHeaders: {
        Authorization: `Bearer ${this.auth.currentToken() ?? ''}`
      },
      onConnect: () => {
        this.client.subscribe(
          environment.stompTopic,
          (msg: IMessage) => {
            try {
              this.attendance$.next(JSON.parse(msg.body) as AttendanceRecord);
            } catch {
              // Ignore malformed frames
            }
          }
        );
      },
      onDisconnect:   () => {},
      reconnectDelay: 5000,
    });

    this.client.activate();
  }

  disconnect(): void {
    if (this.client?.active) {
      this.client.deactivate();
    }
  }

  ngOnDestroy(): void {
    this.disconnect();
  }
}
