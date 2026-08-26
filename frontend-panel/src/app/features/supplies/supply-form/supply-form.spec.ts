import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { SupplyService } from '../supply-service';
import { SupplyForm } from './supply-form';

describe('SupplyForm', () => {
  let component: SupplyForm;
  let fixture: ComponentFixture<SupplyForm>;

  const supplyService = {
    findById: vi.fn(),
    create: vi.fn().mockReturnValue(of({})),
    update: vi.fn().mockReturnValue(of({})),
  };

  beforeEach(async () => {
    supplyService.findById.mockReset();
    supplyService.create.mockReset();
    supplyService.create.mockReturnValue(of({}));
    supplyService.update.mockReset();
    supplyService.update.mockReturnValue(of({}));

    await TestBed.configureTestingModule({
      imports: [SupplyForm],
      providers: [
        provideHttpClient(),
        provideRouter([]),
        { provide: SupplyService, useValue: supplyService },
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

    fixture = TestBed.createComponent(SupplyForm);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('keeps empty form invalid', () => {
    expect(component.form.invalid).toBe(true);
  });
});
