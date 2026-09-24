import { AbstractControl, FormArray, ValidationErrors, ValidatorFn } from '@angular/forms';

/** Suma cantidad por precio unitario de cada ítem del formulario. */
export function itemsTotal(items: FormArray): number {
  return items.controls.reduce(
    (sum, item) =>
      sum + Number(item.get('quantity')?.value ?? 0) * Number(item.get('unitPrice')?.value ?? 0),
    0,
  );
}

/**
 * Un descuento manual (ítem con precio negativo) no puede dejar el total por
 * debajo de cero: el backend lo rechaza, así que se avisa antes de enviar.
 */
export const nonNegativeTotal: ValidatorFn = (control: AbstractControl): ValidationErrors | null =>
  control instanceof FormArray && itemsTotal(control) < 0 ? { negativeTotal: true } : null;
