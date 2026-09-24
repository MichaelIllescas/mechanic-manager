import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { QuoteService } from '../../quotes/quote-service';
import { Quote } from '../../quotes/quote.models';
import { Appointment } from '../appointment.models';
import { AppointmentDetail } from './appointment-detail';

describe('AppointmentDetail', () => {
  let component: AppointmentDetail;
  let fixture: ComponentFixture<AppointmentDetail>;

  const appointment: Appointment = {
    id: 7,
    scheduledAt: '2026-09-10T10:30:00',
    status: 'SCHEDULED',
    notes: 'Cambio de correa',
    vehicleId: 3,
    licensePlate: 'AD123AA',
    brand: 'Fiat',
    model: 'Uno',
    customerId: 2,
    customerFirstName: 'Jose',
    customerLastName: 'Pereyra',
    customerPhone: '1100000000',
    quoteId: 9,
    quoteNumber: 9,
    createdAt: null,
    updatedAt: null,
  };

  const quote: Quote = {
    id: 9,
    number: 9,
    customerId: 2,
    customerName: 'Jose Pereyra',
    customerDocumentNumber: null,
    customerPhone: '1100000000',
    vehicleId: 3,
    vehicleLicensePlate: 'AD123AA',
    vehicleDescription: 'Fiat Uno',
    validUntil: null,
    status: 'APPROVED',
    total: 125000,
    notes: null,
    hasWorkOrder: false,
    items: [],
    createdAt: '2026-09-01T10:00:00',
    updatedAt: '2026-09-01T10:00:00',
  };

  const quoteService = {
    findById: vi.fn().mockReturnValue(of(quote)),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppointmentDetail],
      providers: [provideRouter([]), { provide: QuoteService, useValue: quoteService }],
    }).compileComponents();

    fixture = TestBed.createComponent(AppointmentDetail);
    fixture.componentRef.setInput('appointment', appointment);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('muestra los datos del turno', () => {
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';

    expect(text).toContain('AD123AA');
    expect(text).toContain('Jose Pereyra');
    expect(text).toContain('Cambio de correa');
    expect(text).toContain('$125,000.00');
  });

  it('avisa cuando el usuario cierra el panel', () => {
    const closed = vi.fn();
    component.closed.subscribe(closed);

    const button = (fixture.nativeElement as HTMLElement).querySelector<HTMLButtonElement>(
      '[aria-label="Cerrar detalle"]',
    );
    button?.click();

    expect(closed).toHaveBeenCalled();
  });

  it('emite las acciones del turno con el turno recibido', () => {
    const completed = vi.fn();
    const cancelled = vi.fn();
    component.completed.subscribe(completed);
    component.cancelled.subscribe(cancelled);

    const buttons = Array.from(
      (fixture.nativeElement as HTMLElement).querySelectorAll<HTMLButtonElement>('.detail__button'),
    );
    buttons.find((button) => button.textContent?.includes('Completar'))?.click();
    buttons.find((button) => button.textContent?.includes('Cancelar'))?.click();

    expect(completed).toHaveBeenCalledWith(appointment);
    expect(cancelled).toHaveBeenCalledWith(appointment);
  });

  it('no ofrece acciones sobre un turno ya cerrado', () => {
    fixture.componentRef.setInput('appointment', { ...appointment, status: 'COMPLETED' });
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.textContent).toContain('ya está cerrado');
    expect(element.textContent).toContain('Ver trabajo realizado');
    expect(element.textContent).not.toContain('Reprogramar');
  });
});
