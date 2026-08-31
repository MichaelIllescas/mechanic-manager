import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { VehicleService } from './vehicle-service';
import { CreateVehicleRequest, UpdateVehicleRequest, Vehicle } from './vehicle.models';

describe('VehicleService', () => {
  let service: VehicleService;
  let httpMock: HttpTestingController;

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
    notes: null,
    createdAt: '2026-08-31T10:00:00',
    updatedAt: '2026-08-31T10:00:00',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(VehicleService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should list vehicles without a search parameter', () => {
    service.findAll().subscribe((vehicles) => {
      expect(vehicles).toEqual([vehicle]);
    });

    const request = httpMock.expectOne(`${environment.apiUrl}/vehicles`);
    expect(request.request.method).toBe('GET');
    expect(request.request.params.keys()).toEqual([]);
    request.flush([vehicle]);
  });

  it('should trim and send the search parameter', () => {
    service.findAll('  AB123CD  ').subscribe();

    const request = httpMock.expectOne(
      `${environment.apiUrl}/vehicles?search=AB123CD`,
    );
    expect(request.request.method).toBe('GET');
    request.flush([vehicle]);
  });

  it('should find a vehicle from the authenticated user list', () => {
    service.findFromListById(30).subscribe((found) => {
      expect(found).toEqual(vehicle);
    });

    const request = httpMock.expectOne(`${environment.apiUrl}/vehicles`);
    expect(request.request.method).toBe('GET');
    request.flush([vehicle]);
  });

  it('should create a vehicle with only the API request fields', () => {
    const body: CreateVehicleRequest = {
      customerId: 20,
      licensePlate: 'AB123CD',
      brand: 'Toyota',
      model: 'Corolla',
      year: 2020,
      chassisNumber: 'CHASSIS-123',
      engineNumber: 'ENGINE-123',
      mileage: 45000,
      vehicleType: 'CAR',
      notes: null,
    };

    service.create(body).subscribe((created) => {
      expect(created).toEqual(vehicle);
    });

    const request = httpMock.expectOne(`${environment.apiUrl}/vehicles`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(body);
    expect(request.request.body.userId).toBeUndefined();
    expect(request.request.body.workshopId).toBeUndefined();
    request.flush(vehicle);
  });

  it('should update a vehicle with only the API request fields', () => {
    const body: UpdateVehicleRequest = {
      customerId: 20,
      licensePlate: 'AB123CD',
      brand: 'Toyota',
      model: 'Corolla XEI',
      year: 2020,
      chassisNumber: 'CHASSIS-123',
      engineNumber: 'ENGINE-123',
      mileage: 47000,
      vehicleType: 'CAR',
      notes: 'Service realizado',
    };

    service.update(30, body).subscribe((updated) => {
      expect(updated).toEqual(vehicle);
    });

    const request = httpMock.expectOne(`${environment.apiUrl}/vehicles/30`);
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(body);
    expect(request.request.body.userId).toBeUndefined();
    expect(request.request.body.workshopId).toBeUndefined();
    request.flush(vehicle);
  });
});
