import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { NotificationService } from '../../../core/notifications/notification-service';
import { CustomerService } from '../../customers/customer-service';
import { Customer } from '../../customers/customer.models';
import { VehicleService } from '../vehicle-service';
import { Vehicle } from '../vehicle.models';
import { VehicleForm } from './vehicle-form';

@Component({
  template: '',
})
class TestVehiclesPage {}

describe('VehicleForm', () => {
  let component: VehicleForm;
  let fixture: ComponentFixture<VehicleForm>;
  let routeId: string | null;

  const customer: Customer = {
    id: 20,
    firstName: 'Ana',
    lastName: 'Gómez',
    documentNumber: '30111222',
    phone: '11 4444-5555',
    email: 'ana@email.com',
    notes: null,
    createdAt: null,
    updatedAt: null,
  };

  const vehicle: Vehicle = {
    id: 30,
    customerId: 20,
    customerFirstName: 'Ana',
    customerLastName: 'Gómez',
    licensePlate: 'AB123CD',
    brand: 'Toyota',
    model: 'Corolla',
    year: 2020,
    chassisNumber: 'CHASSIS-123',
    engineNumber: 'ENGINE-123',
    mileage: 45000,
    vehicleType: 'CAR',
    notes: 'Cliente habitual',
    createdAt: null,
    updatedAt: null,
  };

  const customerService = {
    findAll: vi.fn().mockReturnValue(of([customer])),
  };

  const vehicleService = {
    findFromListById: vi.fn().mockReturnValue(of(vehicle)),
    create: vi.fn().mockReturnValue(of(vehicle)),
    update: vi.fn().mockReturnValue(of(vehicle)),
  };

  const notifications = {
    success: vi.fn(),
  };

  beforeEach(async () => {
    routeId = null;
    customerService.findAll.mockReset();
    customerService.findAll.mockReturnValue(of([customer]));
    vehicleService.findFromListById.mockReset();
    vehicleService.findFromListById.mockReturnValue(of(vehicle));
    vehicleService.create.mockReset();
    vehicleService.create.mockReturnValue(of(vehicle));
    vehicleService.update.mockReset();
    vehicleService.update.mockReturnValue(of(vehicle));
    notifications.success.mockReset();

    await TestBed.configureTestingModule({
      imports: [VehicleForm],
      providers: [
        provideRouter([{ path: 'vehicles', component: TestVehiclesPage }]),
        { provide: CustomerService, useValue: customerService },
        { provide: VehicleService, useValue: vehicleService },
        { provide: NotificationService, useValue: notifications },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: () => routeId,
              },
            },
          },
        },
      ],
    }).compileComponents();
  });

  function createComponent(): void {
    fixture = TestBed.createComponent(VehicleForm);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  function fillValidForm(): void {
    component.form.setValue({
      customerId: 20,
      licensePlate: '  ac456ef  ',
      brand: '  Ford  ',
      model: '  Focus  ',
      year: 2021,
      chassisNumber: '  CHASSIS-456  ',
      engineNumber: '  ENGINE-456  ',
      mileage: 32000,
      vehicleType: 'CAR',
      notes: '   ',
    });
  }

  it('should create and load the authenticated user customers', () => {
    createComponent();

    expect(component).toBeTruthy();
    expect(customerService.findAll).toHaveBeenCalledOnce();
    expect(component.customers()).toEqual([customer]);
  });

  it('should not submit an invalid form', () => {
    createComponent();

    component.onSubmit();

    expect(vehicleService.create).not.toHaveBeenCalled();
  });

  it('should create a vehicle with a normalized API payload', () => {
    createComponent();
    fillValidForm();

    component.onSubmit();

    expect(vehicleService.create).toHaveBeenCalledWith({
      customerId: 20,
      licensePlate: 'AC456EF',
      brand: 'Ford',
      model: 'Focus',
      year: 2021,
      chassisNumber: 'CHASSIS-456',
      engineNumber: 'ENGINE-456',
      mileage: 32000,
      vehicleType: 'CAR',
      notes: null,
    });
    expect(notifications.success).toHaveBeenCalledWith('Vehículo registrado correctamente');
  });

  it('should load and update an existing vehicle', () => {
    routeId = '30';
    createComponent();

    expect(vehicleService.findFromListById).toHaveBeenCalledWith(30);
    expect(component.form.controls.customerId.value).toBe(20);
    expect(component.form.controls.licensePlate.value).toBe('AB123CD');

    component.form.controls.mileage.setValue(47000);
    component.onSubmit();

    expect(vehicleService.update).toHaveBeenCalledWith(
      30,
      expect.objectContaining({ mileage: 47000 }),
    );
    expect(notifications.success).toHaveBeenCalledWith('Vehículo actualizado correctamente');
  });
});
