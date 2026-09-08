import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormControl, FormGroup } from '@angular/forms';
import { of } from 'rxjs';

import { CustomerService } from '../../../customers/customer-service';
import { Customer } from '../../../customers/customer.models';
import { VehicleService } from '../../../vehicles/vehicle-service';
import { Vehicle } from '../../../vehicles/vehicle.models';
import { QuotePartyPicker } from './quote-party-picker';

describe('QuotePartyPicker', () => {
  let fixture: ComponentFixture<QuotePartyPicker>;
  let component: QuotePartyPicker;
  const customers = Array.from({ length: 6 }, (_, index) => ({
    id: index + 1,
    firstName: `Cliente ${index + 1}`,
    lastName: 'Prueba',
    documentNumber: null,
    phone: '1144445555',
    email: null,
    address: null,
    notes: null,
    createdAt: null,
    updatedAt: null,
  })) satisfies Customer[];
  const vehicle: Vehicle = {
    id: 8,
    customerId: 1,
    customerFirstName: 'Cliente 1',
    customerLastName: 'Prueba',
    licensePlate: 'AB123CD',
    brand: 'Toyota',
    model: 'Corolla',
    year: 2020,
    chassisNumber: null,
    engineNumber: null,
    mileage: null,
    vehicleType: 'CAR',
    notes: null,
    createdAt: null,
    updatedAt: null,
  };
  const customerService = { findAll: vi.fn(() => of(customers)) };
  const vehicleService = { findAll: vi.fn(() => of([vehicle])) };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [QuotePartyPicker],
      providers: [
        { provide: CustomerService, useValue: customerService },
        { provide: VehicleService, useValue: vehicleService },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(QuotePartyPicker);
    fixture.componentRef.setInput(
      'form',
      new FormGroup({
        customerId: new FormControl<number | null>(null),
        vehicleId: new FormControl<number | null>(null),
      }),
    );
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('shows at most five customer suggestions', async () => {
    await new Promise((resolve) => setTimeout(resolve, 350));
    expect(component.customers()).toHaveLength(5);
  });

  it('selects a vehicle and its owner together', () => {
    component.selectVehicle(vehicle);
    expect(component.vehicleId()).toBe(8);
    expect(component.customerId()).toBe(1);
    expect(component.vehicleLabel()).toContain('AB123CD');
  });

  it('requires a customer before creating a vehicle', () => {
    component.openVehicleCreation();
    expect(component.creationMode()).toBeNull();
    expect(component.errorMessage()).toContain('Primero seleccioná');
  });
});
