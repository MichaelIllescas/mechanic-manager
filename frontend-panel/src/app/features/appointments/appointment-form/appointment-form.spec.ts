import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { NotificationService } from '../../../core/notifications/notification-service';
import { QuoteService } from '../../quotes/quote-service';
import { VehicleService } from '../../vehicles/vehicle-service';
import { Vehicle } from '../../vehicles/vehicle.models';
import { AppointmentService } from '../appointment-service';
import { AppointmentForm } from './appointment-form';

describe('AppointmentForm', () => {
  let fixture: ComponentFixture<AppointmentForm>;
  let component: AppointmentForm;

  const vehicle: Vehicle = {
    id: 30,
    customerId: 20,
    customerFirstName: 'Ana',
    customerLastName: 'Gómez',
    licensePlate: 'AB123CD',
    brand: 'Toyota',
    model: 'Corolla',
    year: null,
    chassisNumber: null,
    engineNumber: null,
    mileage: null,
    vehicleType: 'CAR',
    notes: null,
    createdAt: null,
    updatedAt: null,
  };

  const vehicleService = { findAll: vi.fn().mockReturnValue(of([])) };
  const quoteService = {
    search: vi
      .fn()
      .mockReturnValue(of({ content: [], page: 0, size: 50, totalElements: 0, totalPages: 0 })),
  };
  const appointmentService = {
    findById: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
  };

  beforeEach(async () => {
    vehicleService.findAll.mockReset();
    vehicleService.findAll.mockReturnValue(of([]));
    quoteService.search.mockReset();
    quoteService.search.mockReturnValue(
      of({ content: [], page: 0, size: 50, totalElements: 0, totalPages: 0 }),
    );

    await TestBed.configureTestingModule({
      imports: [AppointmentForm],
      providers: [
        provideRouter([]),
        { provide: VehicleService, useValue: vehicleService },
        { provide: QuoteService, useValue: quoteService },
        { provide: AppointmentService, useValue: appointmentService },
        { provide: NotificationService, useValue: { success: vi.fn() } },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: { get: () => null },
              queryParamMap: { get: () => null },
            },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AppointmentForm);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('offers vehicle registration when there are no vehicles', () => {
    expect(fixture.nativeElement.textContent).toContain('Registrar vehículo');
    expect(fixture.nativeElement.textContent).toContain('Todavía no tenés vehículos registrados');
  });

  it('selects a newly created vehicle without clearing appointment data', () => {
    component.form.patchValue({
      date: '2026-09-24',
      time: '14:30',
      notes: 'Revisar tren delantero',
    });
    component.openVehicleQuickCreate();

    component.vehicleQuickCreated(vehicle);

    expect(component.showVehicleQuickCreate()).toBe(false);
    expect(component.vehicles()).toContain(vehicle);
    expect(component.form.getRawValue()).toEqual({
      vehicleId: 30,
      quoteId: null,
      date: '2026-09-24',
      time: '14:30',
      notes: 'Revisar tren delantero',
    });
    expect(component.selectedVehicleLabel()).toBe('AB123CD — Toyota Corolla');
    expect(component.selectedCustomerId()).toBe(20);
  });
});
