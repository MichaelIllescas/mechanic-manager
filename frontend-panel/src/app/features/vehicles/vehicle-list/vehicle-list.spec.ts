import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { VehicleService } from '../vehicle-service';
import { Vehicle } from '../vehicle.models';
import { VehicleList } from './vehicle-list';

describe('VehicleList', () => {
  let component: VehicleList;
  let fixture: ComponentFixture<VehicleList>;

  const vehicles: Vehicle[] = [
    {
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
      createdAt: null,
      updatedAt: null,
    },
  ];

  const vehicleService = {
    findAll: vi.fn().mockReturnValue(of(vehicles)),
  };

  beforeEach(async () => {
    vehicleService.findAll.mockReset();
    vehicleService.findAll.mockReturnValue(of(vehicles));

    await TestBed.configureTestingModule({
      imports: [VehicleList],
      providers: [provideRouter([]), { provide: VehicleService, useValue: vehicleService }],
    }).compileComponents();

    fixture = TestBed.createComponent(VehicleList);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await waitForSearch();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load vehicles on init', () => {
    expect(vehicleService.findAll).toHaveBeenCalledWith('');
    expect(component.vehicles()).toEqual(vehicles);
  });

  it('should search vehicles after the debounce time', async () => {
    component.searchControl.setValue('AB123CD');

    await waitForSearch();

    expect(vehicleService.findAll).toHaveBeenLastCalledWith('AB123CD');
  });

  it('should render the vehicle and its customer', () => {
    const text = fixture.nativeElement.textContent as string;

    expect(text).toContain('AB123CD');
    expect(text).toContain('#30');
    expect(text).toContain('Toyota Corolla');
    expect(text).toContain('Ana Gómez');
    expect(text).toContain('Auto');
    expect(text).toContain('45000 km');
  });

  it('should clear the current search', () => {
    component.searchControl.setValue('AB123CD');

    component.clearSearch();

    expect(component.searchControl.value).toBe('');
  });

  function waitForSearch(): Promise<void> {
    return new Promise((resolve) => setTimeout(resolve, 350));
  }
});
