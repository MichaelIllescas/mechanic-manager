const FIRST_ERROR_SELECTOR = [
  '[role="alert"]',
  'input.ng-invalid.ng-touched',
  'select.ng-invalid.ng-touched',
  'textarea.ng-invalid.ng-touched',
].join(', ');

/**
 * Lleva la vista al primer error visible del formulario.
 *
 * Los formularios largos muestran el mensaje arriba de todo, y quien toca
 * "Guardar" está abajo: sin esto el error existe pero queda fuera de pantalla.
 * Espera un instante para que Angular termine de dibujar el mensaje.
 */
export function scrollToFirstError(): void {
  setTimeout(() => {
    const target = document.querySelector(FIRST_ERROR_SELECTOR);
    target?.scrollIntoView?.({ behavior: 'smooth', block: 'center' });
  }, 50);
}
