import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { VehicleService } from '../vehicle-service';
import { Vehicle } from '../vehicle.models';
import { VehicleDetail } from './vehicle-detail';

describe('VehicleDetail', () => {
  let component: VehicleDetail;
  let fixture: ComponentFixture<VehicleDetail>;
  let routeId: string | null;

  const vehicle: Vehicle = {
    id: 30,
    customerId: 20,
    customerFirstName: 'Ana',
    customerLastName: 'Gómez',
    licensePlate: 'AB123CD',
    brand: 'Toyota',
    model: 'Corolla',
    year: 2020,
    chassisNumber: null,
    engineNumber: 'ENGINE-123',
    mileage: null,
    vehicleType: 'CAR',
    notes: null,
    createdAt: '2026-08-31T10:00:00',
    updatedAt: '2026-08-31T12:30:00',
  };

  const vehicleService = {
    findFromListById: vi.fn().mockReturnValue(of(vehicle)),
  };

  beforeEach(async () => {
    routeId = '30';
    vehicleService.findFromListById.mockReset();
    vehicleService.findFromListById.mockReturnValue(of(vehicle));

    await TestBed.configureTestingModule({
      imports: [VehicleDetail],
      providers: [
        provideRouter([]),
        { provide: VehicleService, useValue: vehicleService },
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
    fixture = TestBed.createComponent(VehicleDetail);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('should load and render the selected vehicle', () => {
    createComponent();

    const text = fixture.nativeElement.textContent as string;

    expect(vehicleService.findFromListById).toHaveBeenCalledWith(30);
    expect(text).toContain('AB123CD');
    expect(text).toContain('Toyota Corolla');
    expect(text).toContain('Ana Gómez');
    expect(text).toContain('#30');
    expect(text).toContain('Auto');
    expect(text).toContain('ENGINE-123');
    expect(text).toContain('Sin informar');
  });

  it('should show an error for an invalid route id', () => {
    routeId = 'abc';

    createComponent();

    expect(vehicleService.findFromListById).not.toHaveBeenCalled();
    expect(component.errorMessage()).toBe('El identificador del vehículo no es válido');
  });

  it('should show an error when the vehicle does not exist', () => {
    vehicleService.findFromListById.mockReturnValue(of(undefined));

    createComponent();

    expect(component.errorMessage()).toBe('No se encontró el vehículo solicitado');
  });

  it('should show an error when loading fails', () => {
    vehicleService.findFromListById.mockReturnValue(
      throwError(() => ({ error: { message: 'Error de carga' } })),
    );

    createComponent();

    expect(component.errorMessage()).toBe('Error de carga');
  });
});
