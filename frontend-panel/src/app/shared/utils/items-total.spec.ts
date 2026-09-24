import { FormArray, FormControl, FormGroup } from '@angular/forms';

import { itemsTotal, nonNegativeTotal } from './items-total';

function item(quantity: number, unitPrice: number): FormGroup {
  return new FormGroup({
    quantity: new FormControl(quantity),
    unitPrice: new FormControl(unitPrice),
  });
}

describe('items total', () => {
  it('sums quantity times unit price', () => {
    const items = new FormArray([item(2, 1500), item(1, -500)]);

    expect(itemsTotal(items)).toBe(2500);
  });

  it('accepts a discount that keeps the total positive or zero', () => {
    expect(nonNegativeTotal(new FormArray([item(1, 1500), item(1, -500)]))).toBeNull();
    expect(nonNegativeTotal(new FormArray([item(1, 1500), item(1, -1500)]))).toBeNull();
  });

  it('flags a discount larger than the total', () => {
    const items = new FormArray([item(1, 1500), item(1, -4500)]);

    expect(nonNegativeTotal(items)).toEqual({ negativeTotal: true });
  });

  it('is attached to the array and reacts when an item changes', () => {
    const items = new FormArray([item(1, 1500), item(1, -500)], nonNegativeTotal);

    expect(items.valid).toBe(true);

    items.at(1).get('unitPrice')?.setValue(-4500);

    expect(items.hasError('negativeTotal')).toBe(true);
  });
});
