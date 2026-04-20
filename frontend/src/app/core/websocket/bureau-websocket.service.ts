import { Injectable, inject } from '@angular/core';
import { RxStomp } from '@stomp/rx-stomp';
import { filter, map, shareReplay } from 'rxjs/operators';
import { Observable } from 'rxjs';
import type { BureauDashboardDto, SensorAlertDto } from '../models';

@Injectable({
  providedIn: 'root',
})
export class BureauWebsocketService {
  private rxStomp = inject(RxStomp);

  readonly sensorUpdates$: Observable<BureauDashboardDto> = this.rxStomp.watch('/topic/bureau/sensors')
    .pipe(
      filter((msg: any) => !!msg.body),
      map((msg: any) => JSON.parse(msg.body) as BureauDashboardDto),
      shareReplay(1),
    );

  readonly alertUpdates$: Observable<SensorAlertDto> = this.rxStomp.watch('/topic/bureau/alerts')
    .pipe(
      filter((msg: any) => !!msg.body),
      map((msg: any) => JSON.parse(msg.body) as SensorAlertDto),
      shareReplay(1),
    );

  get connected(): boolean {
    return (this.rxStomp as any).connected;
  }

  constructor() {
    // Subscribe to both topics on service initialization to establish subscriptions
    this.sensorUpdates$.subscribe();
    this.alertUpdates$.subscribe();
  }
}

