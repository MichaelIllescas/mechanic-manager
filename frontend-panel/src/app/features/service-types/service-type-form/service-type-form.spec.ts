import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { ServiceTypeService } from '../service-type-service';
import { ServiceTypeForm } from './service-type-form';

describe('ServiceTypeForm', () => {
  let component: ServiceTypeForm;
  let fixture: ComponentFixture<ServiceTypeForm>;

  const serviceTypeService = {
    findById: vi.fn(),
    create: vi.fn().mockReturnValue(of({})),
    update: vi.fn().mockReturnValue(of({})),
  };

  beforeEach(async () => {
    serviceTypeService.findById.mockReset();
    serviceTypeService.create.mockReset();
    serviceTypeService.create.mockReturnValue(of({}));
    serviceTypeService.update.mockReset();
    serviceTypeService.update.mockReturnValue(of({}));

    await TestBed.configureTestingModule({
      imports: [ServiceTypeForm],
      providers: [
        provideHttpClient(),
        provideRouter([]),
        { provide: ServiceTypeService, useValue: serviceTypeService },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: () => null,
              },
            },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ServiceTypeForm);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
