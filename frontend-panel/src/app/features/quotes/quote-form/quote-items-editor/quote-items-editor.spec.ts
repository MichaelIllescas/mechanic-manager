import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormArray } from '@angular/forms';
import { of } from 'rxjs';

import { ServiceTypeService } from '../../../service-types/service-type-service';
import { ServiceType } from '../../../service-types/service-type.models';
import { SupplyService } from '../../../supplies/supply-service';
import { Supply } from '../../../supplies/supply.models';
import { QuoteItemsEditor } from './quote-items-editor';

describe('QuoteItemsEditor', () => {
  let fixture: ComponentFixture<QuoteItemsEditor>;
  let component: QuoteItemsEditor;
  const services = Array.from({ length: 6 }, (_, index) => ({
    id: index + 1,
    name: `Servicio ${index + 1}`,
    description: null,
    basePrice: 10000,
    estimatedMinutes: null,
    active: true,
    createdAt: '',
    updatedAt: '',
  })) satisfies ServiceType[];
  const supplies: Supply[] = [
    { id: 20, name: 'Aceite', price: 2000, active: true, createdAt: '', updatedAt: '' },
  ];
  const serviceTypeService = { findAll: vi.fn(() => of(services)) };
  const supplyService = { findAll: vi.fn(() => of(supplies)) };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [QuoteItemsEditor],
      providers: [
        { provide: ServiceTypeService, useValue: serviceTypeService },
        { provide: SupplyService, useValue: supplyService },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(QuoteItemsEditor);
    fixture.componentRef.setInput('items', new FormArray([]));
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('shows at most five catalog suggestions', () => {
    expect(component.serviceResults()).toHaveLength(5);
  });

  it('adds services and supplies and calculates the total', () => {
    component.addService(services[0]);
    component.addSupply(supplies[0]);
    expect(component.items().length).toBe(2);
    expect(component.total()).toBe(12000);
  });

  it('closes the suggestions when focus leaves the autocomplete', () => {
    const container = document.createElement('section');
    const outside = document.createElement('button');
    component.supplyResultsOpen.set(true);

    component.closeOnFocusOut(
      { currentTarget: container, relatedTarget: outside } as unknown as FocusEvent,
      component.supplyResultsOpen,
    );

    expect(component.supplyResultsOpen()).toBe(false);
  });

  it('keeps the suggestions open when focus moves inside the autocomplete', () => {
    const container = document.createElement('section');
    const option = document.createElement('button');
    container.appendChild(option);
    component.supplyResultsOpen.set(true);

    component.closeOnFocusOut(
      { currentTarget: container, relatedTarget: option } as unknown as FocusEvent,
      component.supplyResultsOpen,
    );

    expect(component.supplyResultsOpen()).toBe(true);
  });

  it('increments an existing catalog item instead of duplicating it', () => {
    component.addService(services[0]);
    component.addService(services[0]);
    expect(component.items().length).toBe(1);
    expect(component.items().at(0).get('quantity')?.value).toBe(2);
  });

  it('accepts a negative price for a manual discount item', () => {
    component.addManualItem();
    const item = component.items().at(0);

    item.patchValue({ description: 'Descuento', quantity: 1, unitPrice: -2500 });

    expect(item.valid).toBe(true);
    expect(component.total()).toBe(-2500);
  });

  it('keeps catalog item prices non-negative', () => {
    component.addService(services[0]);
    const item = component.items().at(0);

    item.get('unitPrice')?.setValue(-1);

    expect(item.get('unitPrice')?.hasError('min')).toBe(true);
  });
});
