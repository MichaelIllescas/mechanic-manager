import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

const VALIDATION_ERROR_CODE = 'VALIDATION_ERROR';

/**
 * El backend responde una validación fallida como
 * {@code { code: 'VALIDATION_ERROR', message: 'Datos inválidos', details: ['campo: mensaje'] }}.
 * El mensaje genérico no le dice a la persona qué corregir; el detalle sí.
 *
 * Se reemplaza el mensaje por el detalle acá, una sola vez, para que cada
 * pantalla siga leyendo {@code error.error.message} y muestre algo útil.
 */
export const validationErrorInterceptor: HttpInterceptorFn = (req, next) =>
  next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      const message = detailedMessage(error);

      if (!message) {
        return throwError(() => error);
      }

      return throwError(
        () =>
          new HttpErrorResponse({
            error: { ...error.error, message },
            headers: error.headers,
            status: error.status,
            statusText: error.statusText,
            url: error.url ?? undefined,
          }),
      );
    }),
  );

function detailedMessage(error: HttpErrorResponse): string | null {
  const body = error.error;

  if (error.status !== 400 || body?.code !== VALIDATION_ERROR_CODE || !Array.isArray(body.details)) {
    return null;
  }

  // "firstName: El nombre es obligatorio" -> "El nombre es obligatorio":
  // el nombre técnico del campo no le sirve a quien completa el formulario.
  const messages = (body.details as unknown[])
    .filter((detail): detail is string => typeof detail === 'string')
    .map((detail) => detail.replace(/^[\w.[\]]+:\s*/, '').trim())
    .filter((detail) => detail.length > 0);

  const unique = [...new Set(messages)];

  return unique.length > 0 ? unique.join('. ') : null;
}
