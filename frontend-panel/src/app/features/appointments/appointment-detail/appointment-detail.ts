import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Appointment } from '../appointment.models';

/**
 * Panel lateral con el detalle de un turno.
 *
 * No conoce el calendario ni el servicio: recibe el turno y avisa qué quiso
 * hacer el usuario, para que la vista que lo abrió decida cómo resolverlo.
 */
@Component({
  selector: 'app-appointment-detail',
  imports: [RouterLink],
  templateUrl: './appointment-detail.html',
  styleUrl: './appointment-detail.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    '(document:keydown.escape)': 'closed.emit()',
  },
})
export class AppointmentDetail {
  readonly appointment = input.required<Appointment>();

  readonly closed = output<void>();
  readonly completed = output<Appointment>();
  readonly cancelled = output<Appointment>();
}
