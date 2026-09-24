import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { catchError, of } from 'rxjs';

import { ConfirmService } from '../../../core/confirm/confirm-service';
import { NotificationService } from '../../../core/notifications/notification-service';
import { AppointmentAgendaList } from '../appointment-agenda-list/appointment-agenda-list';
import { AppointmentDetail } from '../appointment-detail/appointment-detail';
import { AppointmentService } from '../appointment-service';
import {
  APPOINTMENT_STATUS_FILTERS,
  APPOINTMENT_STATUS_LEGEND,
  AppointmentStatusFilter,
  filterAppointmentsByStatus,
} from '../appointment-status-filter';
import { Appointment, CalendarView } from '../appointment.models';
import {
  daysFor,
  isSameDay,
  minutesFromMidnight,
  parseLocalDateTime,
  rangeFor,
  shift,
  stackOverlapping,
  startOfDay,
  toIsoDate,
} from '../calendar.utils';

/** Alto en píxeles de cada hora de la grilla. Debe coincidir con el CSS. */
const HOUR_HEIGHT = 56;

/**
 * Ancho a partir del cual entra la grilla semanal.
 *
 * Debajo de esto las siete columnas quedan de menos de 50px y la semana pasa a
 * mostrarse como lista. Coincide con el breakpoint del CSS.
 */
const WIDE_SCREEN = '(min-width: 700px)';

/** Horario de taller que la grilla muestra siempre, haya turnos o no. */
const DEFAULT_FIRST_HOUR = 7;
const DEFAULT_LAST_HOUR = 20;

/** Turnos que entran en una celda del mes antes de resumir el resto. */
const MONTH_CELL_LIMIT = 3;

/**
 * Duración visual de un turno en la grilla horaria.
 *
 * El turno no tiene duración en el modelo: sólo sabemos cuándo empieza. Este
 * valor define el alto del bloque y, con él, qué turnos se consideran
 * simultáneos para repartirlos en columnas.
 */
const EVENT_MINUTES = 60;

interface PositionedAppointment {
  appointment: Appointment;
  /** Distancia desde el tope de la grilla, en píxeles. */
  top: number;
  height: number;
  time: string;
}

interface DayColumn {
  date: Date;
  isoDate: string;
  isToday: boolean;
  appointments: PositionedAppointment[];
}

interface MonthCell {
  date: Date;
  isoDate: string;
  dayNumber: number;
  isToday: boolean;
  inCurrentMonth: boolean;
  visible: PositionedAppointment[];
  hiddenCount: number;
}

@Component({
  selector: 'app-appointment-calendar',
  imports: [RouterLink, AppointmentDetail, AppointmentAgendaList],
  templateUrl: './appointment-calendar.html',
  styleUrl: './appointment-calendar.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppointmentCalendar implements OnInit {
  private readonly appointmentService = inject(AppointmentService);
  private readonly confirmService = inject(ConfirmService);
  private readonly notifications = inject(NotificationService);
  private readonly destroyRef = inject(DestroyRef);

  readonly hourHeight = HOUR_HEIGHT;
  readonly weekdayNames = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];

  private readonly wideScreen =
    typeof window !== 'undefined' && window.matchMedia ? window.matchMedia(WIDE_SCREEN) : null;

  /** El teléfono no da para la grilla semanal: arranca en la vista del día. */
  readonly isWide = signal(this.wideScreen?.matches ?? true);

  readonly view = signal<CalendarView>(this.isWide() ? 'week' : 'day');
  readonly anchor = signal(startOfDay(new Date()));
  readonly appointments = signal<Appointment[]>([]);
  readonly statusFilter = signal<AppointmentStatusFilter>('ALL');
  readonly statusFilters = APPOINTMENT_STATUS_FILTERS;
  readonly statusLegend = APPOINTMENT_STATUS_LEGEND;
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly selected = signal<Appointment | null>(null);
  readonly filteredAppointments = computed(() =>
    filterAppointmentsByStatus(this.appointments(), this.statusFilter()),
  );

  /**
   * Franja de horas que dibuja la grilla.
   *
   * La grilla no muestra las 24 horas: arrancar a la medianoche obligaba a
   * scrollear la grilla por dentro para llegar a la mañana. Cubre el horario
   * de taller y se estira sola si hay un turno fuera de ese rango, así nunca
   * esconde nada.
   */
  private readonly hourRange = computed(() => {
    let from = DEFAULT_FIRST_HOUR;
    let to = DEFAULT_LAST_HOUR;

    for (const appointment of this.filteredAppointments()) {
      const hour = parseLocalDateTime(appointment.scheduledAt).getHours();
      from = Math.min(from, hour);
      to = Math.max(to, hour + 1);
    }

    return { from, to };
  });

  readonly hours = computed(() => {
    const { from, to } = this.hourRange();
    return Array.from({ length: to - from }, (_, index) => from + index);
  });

  /** Píxeles que hay que descontar por las horas que la grilla no dibuja. */
  readonly gridOffset = computed(() => this.hourRange().from * HOUR_HEIGHT);

  readonly gridHeight = computed(() => this.hours().length * HOUR_HEIGHT);

  /** Turnos agrupados por día, para no recorrer la lista entera por celda. */
  private readonly byDay = computed(() => {
    const groups = new Map<string, PositionedAppointment[]>();

    for (const appointment of this.filteredAppointments()) {
      const date = parseLocalDateTime(appointment.scheduledAt);
      const key = toIsoDate(date);
      const positioned: PositionedAppointment = {
        appointment,
        top: (minutesFromMidnight(date) / 60) * HOUR_HEIGHT,
        height: (EVENT_MINUTES / 60) * HOUR_HEIGHT,
        time: date.toLocaleTimeString('es-AR', {
          hour: '2-digit',
          minute: '2-digit',
          hour12: false,
        }),
      };

      const existing = groups.get(key);
      if (existing) {
        existing.push(positioned);
      } else {
        groups.set(key, [positioned]);
      }
    }

    // Dos turnos a la misma hora son normales: el taller atiende varios autos
    // a la vez. Sin esto quedarían encimados y sólo se vería el último.
    for (const items of groups.values()) {
      stackOverlapping(items);
    }

    return groups;
  });

  readonly columns = computed<DayColumn[]>(() => {
    const today = new Date();

    return daysFor(this.view(), this.anchor()).map((date) => {
      const isoDate = toIsoDate(date);

      return {
        date,
        isoDate,
        isToday: isSameDay(date, today),
        appointments: this.byDay().get(isoDate) ?? [],
      };
    });
  });

  readonly monthCells = computed<MonthCell[]>(() => {
    const month = this.anchor().getMonth();

    return this.columns().map((column) => {
      const all = column.appointments;

      return {
        date: column.date,
        isoDate: column.isoDate,
        dayNumber: column.date.getDate(),
        isToday: column.isToday,
        inCurrentMonth: column.date.getMonth() === month,
        visible: all.slice(0, MONTH_CELL_LIMIT),
        hiddenCount: Math.max(0, all.length - MONTH_CELL_LIMIT),
      };
    });
  });

  readonly title = computed(() => {
    const anchor = this.anchor();

    // En el teléfono el título va abreviado: entero se come tres renglones.
    const wide = this.isWide();

    switch (this.view()) {
      case 'day':
        return anchor.toLocaleDateString('es-AR', {
          weekday: wide ? 'long' : 'short',
          day: 'numeric',
          month: wide ? 'long' : 'short',
          year: 'numeric',
        });

      case 'week': {
        const days = this.columns();
        const first = days[0].date;
        const last = days[days.length - 1].date;
        const sameMonth = first.getMonth() === last.getMonth();

        const firstLabel = first.toLocaleDateString('es-AR', {
          day: 'numeric',
          month: sameMonth ? undefined : 'short',
        });
        const lastLabel = last.toLocaleDateString('es-AR', {
          day: 'numeric',
          month: 'short',
          year: 'numeric',
        });

        return `${firstLabel} – ${lastLabel}`;
      }

      case 'month':
        return anchor.toLocaleDateString('es-AR', {
          month: wide ? 'long' : 'short',
          year: 'numeric',
        });
    }
  });

  /**
   * Día que propone el alta de turno: el que estás mirando, salvo en la vista
   * mensual, donde el ancla es un mes entero y "hoy" es mejor sugerencia.
   */
  readonly newAppointmentDate = computed(() =>
    toIsoDate(this.view() === 'month' ? new Date() : this.anchor()),
  );

  ngOnInit(): void {
    const onResize = (event: MediaQueryListEvent) => this.isWide.set(event.matches);
    this.wideScreen?.addEventListener('change', onResize);
    this.destroyRef.onDestroy(() => this.wideScreen?.removeEventListener('change', onResize));

    this.load();
  }

  changeView(view: CalendarView): void {
    if (view === this.view()) {
      return;
    }

    this.view.set(view);
    this.selected.set(null);
    this.load();
  }

  setStatusFilter(filter: AppointmentStatusFilter): void {
    if (filter === this.statusFilter()) {
      return;
    }

    this.statusFilter.set(filter);

    const selected = this.selected();
    if (selected && filter !== 'ALL' && selected.status !== filter) {
      this.selected.set(null);
    }
  }

  move(direction: 1 | -1): void {
    this.anchor.set(shift(this.view(), this.anchor(), direction));
    this.selected.set(null);
    this.load();
  }

  goToToday(): void {
    this.anchor.set(startOfDay(new Date()));
    this.selected.set(null);
    this.load();
  }

  /** Desde el mes, clickear un día abre ese día en la vista diaria. */
  openDay(date: Date): void {
    this.anchor.set(startOfDay(date));
    this.view.set('day');
    this.selected.set(null);
    this.load();
  }

  select(appointment: Appointment): void {
    this.selected.set(this.selected()?.id === appointment.id ? null : appointment);
  }

  closeDetail(): void {
    this.selected.set(null);
  }

  complete(appointment: Appointment): void {
    this.runAction(this.appointmentService.complete(appointment.id));
  }

  cancel(appointment: Appointment): void {
    this.runAction(this.appointmentService.cancel(appointment.id));
  }

  async delete(appointment: Appointment, event?: MouseEvent): Promise<void> {
    // El ícono va dentro de la misma tarjeta clickeable: sin esto, el click
    // también dispara select() y abre el detalle del turno que se está borrando.
    event?.stopPropagation();

    const confirmed = await this.confirmService.confirm({
      title: 'Eliminar turno',
      message: `¿Eliminar el turno de ${appointment.licensePlate} (${appointment.brand} ${appointment.model})? Esta acción no se puede deshacer.`,
      confirmLabel: 'Eliminar',
      danger: true,
    });
    if (!confirmed) {
      return;
    }

    this.appointmentService.delete(appointment.id).subscribe({
      next: () => {
        this.appointments.update((current) => current.filter((item) => item.id !== appointment.id));
        if (this.selected()?.id === appointment.id) {
          this.selected.set(null);
        }
        this.notifications.success('Turno eliminado correctamente');
      },
      error: (error: HttpErrorResponse) => {
        this.notifications.error(error.error?.message ?? 'No se pudo eliminar el turno');
      },
    });
  }

  private runAction(action: ReturnType<AppointmentService['complete']>): void {
    this.errorMessage.set(null);

    action
      .pipe(
        catchError((error: HttpErrorResponse) => {
          this.errorMessage.set(error.error?.message ?? 'No se pudo actualizar el turno');
          return of(null);
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((updated) => {
        if (!updated) {
          return;
        }

        this.appointments.update((current) =>
          current.map((item) => (item.id === updated.id ? updated : item)),
        );
        const filter = this.statusFilter();
        this.selected.set(filter === 'ALL' || updated.status === filter ? updated : null);
      });
  }

  private load(): void {
    const { from, to } = rangeFor(this.view(), this.anchor());

    this.loading.set(true);
    this.errorMessage.set(null);

    this.appointmentService
      .findByRange(from, to)
      .pipe(
        catchError((error: HttpErrorResponse) => {
          this.errorMessage.set(error.error?.message ?? 'No se pudo cargar la agenda');
          return of([]);
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((appointments) => {
        this.appointments.set(appointments);
        this.loading.set(false);
      });
  }
}
