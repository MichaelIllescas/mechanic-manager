import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import { NotificationService } from '../../../../core/notifications/notification-service';
import { CustomerService } from '../../../customers/customer-service';
import { Customer } from '../../../customers/customer.models';
import { VehicleService } from '../../../vehicles/vehicle-service';
import { Vehicle } from '../../../vehicles/vehicle.models';
import { VehicleQuickCreate } from './vehicle-quick-create';

describe('VehicleQuickCreate', () => {
  let fixture: ComponentFixture<VehicleQuickCreate>;
  let component: VehicleQuickCreate;

  const customer: Customer = {
    id: 20,
    firstName: 'Ana',
    lastName: 'Gómez',
    documentNumber: null,
    phone: '11 4444-5555',
    email: null,
    address: null,
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
    year: null,
    chassisNumber: null,
    engineNumber: null,
    mileage: null,
    vehicleType: 'CAR',
    notes: null,
    createdAt: null,
    updatedAt: null,
  };

  const customerService = {
    findAll: vi.fn().mockReturnValue(of([customer])),
    create: vi.fn().mockReturnValue(of(customer)),
  };
  const vehicleService = { create: vi.fn().mockReturnValue(of(vehicle)) };
  const notifications = { success: vi.fn() };

  beforeEach(async () => {
    customerService.findAll.mockReset();
    customerService.findAll.mockReturnValue(of([customer]));
    customerService.create.mockReset();
    customerService.create.mockReturnValue(of(customer));
    vehicleService.create.mockReset();
    vehicleService.create.mockReturnValue(of(vehicle));
    notifications.success.mockReset();

    await TestBed.configureTestingModule({
      imports: [VehicleQuickCreate],
      providers: [
        { provide: CustomerService, useValue: customerService },
        { provide: VehicleService, useValue: vehicleService },
        { provide: NotificationService, useValue: notifications },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(VehicleQuickCreate);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('loads existing customers', () => {
    expect(customerService.findAll).toHaveBeenCalledOnce();
    expect(component.customers()).toEqual([customer]);
  });

  it('creates and selects a customer without clearing vehicle data', () => {
    component.vehicleForm.patchValue({ licensePlate: 'AB123CD', brand: 'Toyota' });
    component.startCustomerCreation();
    component.customerForm.setValue({
      firstName: 'Ana',
      lastName: 'Gómez',
      phone: '11 4444-5555',
    });

    component.saveCustomer();

    expect(customerService.create).toHaveBeenCalledWith({
      firstName: 'Ana',
      lastName: 'Gómez',
      phone: '11 4444-5555',
      documentNumber: null,
      email: null,
      address: null,
      notes: null,
    });
    expect(component.selectedCustomer()).toEqual(customer);
    expect(component.vehicleForm.controls.licensePlate.value).toBe('AB123CD');
    expect(component.vehicleForm.controls.brand.value).toBe('Toyota');
  });

  it('registers and emits a normalized vehicle', () => {
    const saved = vi.fn();
    component.saved.subscribe(saved);
    component.selectCustomer(customer);
    component.vehicleForm.setValue({
      licensePlate: '  ab123cd ',
      vehicleType: 'CAR',
      brand: ' Toyota ',
      model: ' Corolla ',
    });

    component.saveVehicle();

    expect(vehicleService.create).toHaveBeenCalledWith({
      customerId: 20,
      licensePlate: 'AB123CD',
      vehicleType: 'CAR',
      brand: 'Toyota',
      model: 'Corolla',
      year: null,
      chassisNumber: null,
      engineNumber: null,
      mileage: null,
      notes: null,
    });
    expect(saved).toHaveBeenCalledWith(vehicle);
  });

  it('requires a selected customer before creating the vehicle', () => {
    component.vehicleForm.setValue({
      licensePlate: 'AB123CD',
      vehicleType: 'CAR',
      brand: 'Toyota',
      model: 'Corolla',
    });

    component.saveVehicle();

    expect(vehicleService.create).not.toHaveBeenCalled();
    expect(component.errorMessage()).toContain('cliente');
  });

  it('keeps the modal open and shows the API error', () => {
    vehicleService.create.mockReturnValue(
      throwError(() => ({ error: { message: 'La patente ya está registrada' } })),
    );
    component.selectCustomer(customer);
    component.vehicleForm.setValue({
      licensePlate: 'AB123CD',
      vehicleType: 'CAR',
      brand: 'Toyota',
      model: 'Corolla',
    });

    component.saveVehicle();

    expect(component.errorMessage()).toBe('La patente ya está registrada');
    expect(component.savingVehicle()).toBe(false);
  });
});
