import { describe, expect, it } from 'vitest';

import { Appointment } from './appointment.models';
import {
  APPOINTMENT_STATUS_FILTERS,
  APPOINTMENT_STATUS_LEGEND,
  filterAppointmentsByStatus,
} from './appointment-status-filter';

describe('appointment status filter', () => {
  const appointments = [
    appointment({ id: 1, status: 'SCHEDULED' }),
    appointment({ id: 2, status: 'SCHEDULED' }),
    appointment({ id: 3, status: 'COMPLETED' }),
    appointment({ id: 4, status: 'CANCELLED' }),
  ];

  it('provides one option for all appointments and one per status', () => {
    expect(APPOINTMENT_STATUS_FILTERS.map((option) => option.value)).toEqual([
      'ALL',
      'SCHEDULED',
      'COMPLETED',
      'CANCELLED',
    ]);
  });

  it('provides a color legend for every appointment status', () => {
    expect(APPOINTMENT_STATUS_LEGEND).toEqual([
      { value: 'SCHEDULED', label: 'Pendiente' },
      { value: 'COMPLETED', label: 'Completado' },
      { value: 'CANCELLED', label: 'Cancelado' },
    ]);
  });

  it('returns every appointment when all is selected', () => {
    expect(filterAppointmentsByStatus(appointments, 'ALL')).toEqual(appointments);
  });

  it('returns only appointments with the selected status', () => {
    expect(filterAppointmentsByStatus(appointments, 'SCHEDULED').map((item) => item.id)).toEqual([
      1, 2,
    ]);
  });
});

function appointment(overrides: Partial<Appointment>): Appointment {
  return {
    id: 1,
    scheduledAt: '2026-09-10T10:30:00',
    status: 'SCHEDULED',
    notes: null,
    vehicleId: 1,
    licensePlate: 'AA123AA',
    brand: 'Fiat',
    model: 'Uno',
    customerId: 1,
    customerFirstName: 'Jose',
    customerLastName: 'Pereyra',
    customerPhone: '1100000000',
    quoteId: null,
    quoteNumber: null,
    createdAt: null,
    updatedAt: null,
    ...overrides,
  };
}
