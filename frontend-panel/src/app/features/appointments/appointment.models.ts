export type AppointmentStatus = 'SCHEDULED' | 'COMPLETED' | 'CANCELLED';

export interface Appointment {
  id: number;
  /** ISO local sin zona, tal como lo emite el backend: 2026-09-10T10:30:00 */
  scheduledAt: string;
  status: AppointmentStatus;
  notes: string | null;
  vehicleId: number;
  licensePlate: string;
  brand: string;
  model: string;
  customerId: number;
  customerFirstName: string;
  customerLastName: string;
  createdAt: string | null;
  updatedAt: string | null;
}

export interface CreateAppointmentRequest {
  vehicleId: number;
  scheduledAt: string;
  notes: string | null;
}

export interface UpdateAppointmentRequest {
  scheduledAt: string;
  notes: string | null;
}

/** Vistas del calendario. Las tres consultan el mismo endpoint por rango. */
export type CalendarView = 'day' | 'week' | 'month';
