import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { ServiceTypeService } from '../service-type-service';
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
});
