import { Component, OnInit, inject, ViewChild } from '@angular/core';
import { CommonModule }           from '@angular/common';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatButtonModule }        from '@angular/material/button';
import { MatIconModule }          from '@angular/material/icon';
import { MatCardModule }          from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog }              from '@angular/material/dialog';
import { MatTooltipModule }       from '@angular/material/tooltip';
import { MatSnackBar }            from '@angular/material/snack-bar';
import { FullCalendarModule }     from '@fullcalendar/angular';
import { CalendarOptions, EventInput, EventSourceInput } from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import googleCalendarPlugin from '@fullcalendar/google-calendar';
import { PlanningApiService }     from '../../core/api/planning.service';
import { PlanningFormComponent }  from './planning-form/planning-form.component';
import type { Planning }          from '../../core/models';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-planning',
  templateUrl: './planning.component.html',
  styleUrl:    './planning.component.scss',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule, MatPaginatorModule, MatButtonModule,
    MatIconModule, MatCardModule, MatProgressSpinnerModule, MatTooltipModule, FullCalendarModule,
  ],
})
export class PlanningComponent implements OnInit {
  private api    = inject(PlanningApiService);
  private dialog = inject(MatDialog);
  private snack  = inject(MatSnackBar);

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  dataSource    = new MatTableDataSource<Planning>();
  loading       = true;
  totalElements = 0;
  pageSize      = 20;
  displayedCols = ['employe', 'type', 'horaires', 'dateDebut', 'dateFin', 'actions'];
  readonly googleCalendarEnabled = !!(environment.googleCalendarApiKey && environment.googleCalendarId);

  calendarOptions: CalendarOptions = {
    plugins: [dayGridPlugin, interactionPlugin, googleCalendarPlugin],
    initialView: 'dayGridMonth',
    firstDay: 1,
    locale: 'fr',
    fixedWeekCount: false,
    nowIndicator: true,
    height: 'auto',
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth'
    },
    eventDisplay: 'block',
    eventTimeFormat: { hour: '2-digit', minute: '2-digit', meridiem: false },
    googleCalendarApiKey: environment.googleCalendarApiKey || undefined,
    eventSources: [],
  };

  ngOnInit(): void { this.load(0); }

  load(page: number): void {
    this.loading = true;
    this.api.getAll({ page, size: this.pageSize }).subscribe({
      next: p => {
        this.dataSource.data = p.content;
        this.totalElements = p.totalElements;
        this.refreshCalendarSources();
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  onPage(e: PageEvent): void { this.load(e.pageIndex); }

  openForm(planning?: Planning): void {
    this.dialog.open(PlanningFormComponent, { width: '560px', data: { planning } })
      .afterClosed().subscribe((saved: Planning | undefined) => {
        if (!saved) return;
        if (planning) {
          const data = [...this.dataSource.data];
          const idx  = data.findIndex(p => p.id === saved.id);
          if (idx >= 0) {
            data[idx] = saved;
            this.dataSource.data = data;
            this.refreshCalendarSources();
          }
        } else {
          this.load(0);
        }
      });
  }

  delete(p: Planning): void {
    if (!confirm('Supprimer cette entree planning ?')) return;
    this.api.delete(p.id).subscribe({
      next: () => {
        this.dataSource.data = this.dataSource.data.filter(x => x.id !== p.id);
        this.refreshCalendarSources();
        this.snack.open('Entree supprimee.', 'OK', { duration: 3000 });
      },
      error: () => this.snack.open('Erreur lors de la suppression.', 'OK', { duration: 3000 }),
    });
  }

  private refreshCalendarSources(): void {
    const sources: EventSourceInput[] = [
      {
        id: 'planning-source',
        events: this.toPlanningEvents(this.dataSource.data),
        color: '#0b6a5f',
        textColor: '#ffffff'
      }
    ];

    if (this.googleCalendarEnabled) {
      sources.push({
        googleCalendarId: environment.googleCalendarId,
        className: 'google-calendar-source'
      });
    }

    this.calendarOptions = {
      ...this.calendarOptions,
      eventSources: sources,
      googleCalendarApiKey: environment.googleCalendarApiKey || undefined,
    };
  }

  private toPlanningEvents(items: Planning[]): EventInput[] {
    return items
      .map((p) => {
        if (!p.dateDebut && !p.dateFin) return null;
        const startDate = p.dateDebut || p.dateFin;
        const endDateRaw = p.dateFin || p.dateDebut;
        const endExclusive = endDateRaw ? this.addOneDay(endDateRaw) : undefined;
        return {
          id: String(p.id),
          title: `${p.type ?? 'Planning'} - ${p.employeNomComplet ?? `Employe #${p.employeId}`}`,
          start: startDate || undefined,
          end: endExclusive,
          allDay: true,
        } as EventInput;
      })
      .filter((e): e is EventInput => !!e);
  }

  private addOneDay(isoDate: string): string {
    const date = new Date(`${isoDate}T00:00:00`);
    if (Number.isNaN(date.getTime())) return isoDate;
    date.setDate(date.getDate() + 1);
    return date.toISOString().slice(0, 10);
  }
}
