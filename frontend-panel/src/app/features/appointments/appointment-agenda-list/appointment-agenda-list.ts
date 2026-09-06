import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

import { Appointment } from '../appointment.models';

/** Un día de la agenda con los turnos que tiene, ya ordenados por horario. */
export interface AgendaDay {
  date: Date;
  isoDate: string;
  isToday: boolean;
  appointments: { appointment: Appointment; time: string }[];
}

/**
 * La semana como lista en vez de grilla.
 *
 * En un teléfono las siete columnas de la grilla quedan de menos de 50px: no
 * entra ni la patente. La lista muestra lo mismo, un día abajo del otro, con
 * el ancho completo de la pantalla para cada turno.
 */
@Component({
  selector: 'app-appointment-agenda-list',
  templateUrl: './appointment-agenda-list.html',
  styleUrl: './appointment-agenda-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppointmentAgendaList {
  readonly days = input.required<AgendaDay[]>();
  readonly selectedId = input<number | null>(null);

  readonly picked = output<Appointment>();

  dayLabel(date: Date): string {
    return date.toLocaleDateString('es-AR', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
    });
  }
}
