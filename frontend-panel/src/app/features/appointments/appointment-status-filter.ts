import { Appointment, AppointmentStatus } from './appointment.models';

export type AppointmentStatusFilter = 'ALL' | AppointmentStatus;

export interface AppointmentStatusFilterOption {
  value: AppointmentStatusFilter;
  label: string;
}

export const APPOINTMENT_STATUS_FILTERS: readonly AppointmentStatusFilterOption[] = [
  { value: 'ALL', label: 'Todos' },
  { value: 'SCHEDULED', label: 'Agendados' },
  { value: 'COMPLETED', label: 'Completados' },
  { value: 'CANCELLED', label: 'Cancelados' },
];

export function filterAppointmentsByStatus(
  appointments: Appointment[],
  filter: AppointmentStatusFilter,
): Appointment[] {
  if (filter === 'ALL') {
    return appointments;
  }

  return appointments.filter((appointment) => appointment.status === filter);
}
