import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { ServiceTypeService } from '../service-type-service';
import { ServiceType } from '../service-type.models';
import { ServiceTypeList } from './service-type-list';

describe('ServiceTypeList', () => {
  let component: ServiceTypeList;
  let fixture: ComponentFixture<ServiceTypeList>;

  const serviceTypeService = {
    findAll: vi.fn().mockReturnValue(of([])),
    updateStatus: vi.fn(),
  };

  beforeEach(async () => {
    serviceTypeService.findAll.mockReset();
    serviceTypeService.findAll.mockReturnValue(of([]));
    serviceTypeService.updateStatus.mockReset();

    await TestBed.configureTestingModule({
      imports: [ServiceTypeList],
      providers: [
        provideHttpClient(),
        provideRouter([]),
        { provide: ServiceTypeService, useValue: serviceTypeService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ServiceTypeList);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('filters services by name or description', () => {
    component.serviceTypes.set([
      serviceType({ id: 1, name: 'Cambio de aceite', description: 'Motor y filtro' }),
      serviceType({ id: 2, name: 'Alineacion', description: 'Tren delantero' }),
      serviceType({ id: 3, name: 'Revision general', description: 'Chequeo de motor' }),
    ]);

    component.searchControl.setValue('motor');

    expect(component.filteredServiceTypes().map((serviceType) => serviceType.id)).toEqual([1, 3]);
  });

  it('clears the service search', () => {
    component.serviceTypes.set([
      serviceType({ id: 1, name: 'Cambio de aceite' }),
      serviceType({ id: 2, name: 'Alineacion' }),
    ]);
    component.searchControl.setValue('aceite');

    component.clearSearch();

    expect(component.searchControl.value).toBe('');
    expect(component.filteredServiceTypes()).toHaveLength(2);
  });
});

function serviceType(overrides: Partial<ServiceType>): ServiceType {
  return {
    id: 1,
    name: 'Servicio',
    description: null,
    basePrice: 1000,
    active: true,
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
    ...overrides,
  };
}
