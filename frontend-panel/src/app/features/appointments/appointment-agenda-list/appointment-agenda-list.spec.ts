import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Appointment } from '../appointment.models';
import { AgendaDay, AppointmentAgendaList } from './appointment-agenda-list';

describe('AppointmentAgendaList', () => {
  let component: AppointmentAgendaList;
  let fixture: ComponentFixture<AppointmentAgendaList>;

  const appointment = (id: number, plate: string): Appointment => ({
    id,
    scheduledAt: '2026-09-10T10:30:00',
    status: 'SCHEDULED',
    notes: null,
    vehicleId: id,
    licensePlate: plate,
    brand: 'Fiat',
    model: 'Uno',
    customerId: 1,
    customerFirstName: 'Jose',
    customerLastName: 'Pereyra',
    createdAt: null,
    updatedAt: null,
  });

  const days: AgendaDay[] = [
    {
      date: new Date(2026, 8, 10),
      isoDate: '2026-09-10',
      isToday: true,
      appointments: [
        { appointment: appointment(1, 'AD123AA'), time: '10:30' },
        { appointment: appointment(2, 'BC456BB'), time: '11:00' },
      ],
    },
    {
      date: new Date(2026, 8, 11),
      isoDate: '2026-09-11',
      isToday: false,
      appointments: [],
    },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [AppointmentAgendaList] }).compileComponents();

    fixture = TestBed.createComponent(AppointmentAgendaList);
    fixture.componentRef.setInput('days', days);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('lista los turnos de cada día con hora y patente', () => {
    const items = (fixture.nativeElement as HTMLElement).querySelectorAll('.agenda-list__item');

    expect(items.length).toBe(2);
    expect(items[0].textContent).toContain('10:30');
    expect(items[0].textContent).toContain('AD123AA');
  });

  it('avisa que un día no tiene turnos en vez de dejarlo vacío', () => {
    const empties = (fixture.nativeElement as HTMLElement).querySelectorAll('.agenda-list__empty');

    expect(empties.length).toBe(1);
    expect(empties[0].textContent).toContain('Sin turnos');
  });

  it('emite el turno que el usuario toca', () => {
    const picked = vi.fn();
    component.picked.subscribe(picked);

    (fixture.nativeElement as HTMLElement)
      .querySelectorAll<HTMLButtonElement>('.agenda-list__item')[1]
      .click();

    expect(picked).toHaveBeenCalledWith(days[0].appointments[1].appointment);
  });

  it('marca el turno abierto en el detalle', () => {
    fixture.componentRef.setInput('selectedId', 2);
    fixture.detectChanges();

    const selected = (fixture.nativeElement as HTMLElement).querySelectorAll('.is-selected');

    expect(selected.length).toBe(1);
    expect(selected[0].textContent).toContain('BC456BB');
  });
});
