import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { SupplyService } from '../supply-service';
import { Supply } from '../supply.models';
import { SupplyList } from './supply-list';

describe('SupplyList', () => {
  let component: SupplyList;
  let fixture: ComponentFixture<SupplyList>;

  const supplyService = {
    findAll: vi.fn().mockReturnValue(of([])),
    updateStatus: vi.fn(),
  };

  beforeEach(async () => {
    supplyService.findAll.mockReset();
    supplyService.findAll.mockReturnValue(of([]));
    supplyService.updateStatus.mockReset();

    await TestBed.configureTestingModule({
      imports: [SupplyList],
      providers: [
        provideHttpClient(),
        provideRouter([]),
        { provide: SupplyService, useValue: supplyService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SupplyList);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads all supplies by default', () => {
    expect(supplyService.findAll).toHaveBeenCalledWith(undefined);
  });

  it('filters supplies by name', () => {
    component.supplies.set([
      supply({ id: 1, name: 'Aceite sintetico' }),
      supply({ id: 2, name: 'Filtro de aire' }),
      supply({ id: 3, name: 'Filtro de aceite' }),
    ]);

    component.searchControl.setValue('aceite');

    expect(component.filteredSupplies().map((item) => item.id)).toEqual([1, 3]);
  });

  it('clears the supply search', () => {
    component.supplies.set([
      supply({ id: 1, name: 'Aceite sintetico' }),
      supply({ id: 2, name: 'Filtro de aire' }),
    ]);
    component.searchControl.setValue('aceite');

    component.clearSearch();

    expect(component.searchControl.value).toBe('');
    expect(component.filteredSupplies()).toHaveLength(2);
  });
});

function supply(overrides: Partial<Supply>): Supply {
  return {
    id: 1,
    name: 'Insumo',
    price: 1000,
    active: true,
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
    ...overrides,
  };
}
