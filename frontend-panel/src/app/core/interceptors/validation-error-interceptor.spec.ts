import { HttpClient, HttpErrorResponse, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { validationErrorInterceptor } from './validation-error-interceptor';

describe('validationErrorInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([validationErrorInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
  });

  function failWith(status: number, body: object | string): HttpErrorResponse {
    let captured!: HttpErrorResponse;
    http.get('/api/x').subscribe({ error: (error: HttpErrorResponse) => (captured = error) });
    controller.expectOne('/api/x').flush(body, { status, statusText: 'err' });
    return captured;
  }

  it('replaces the generic message with the field messages', () => {
    const error = failWith(400, {
      code: 'VALIDATION_ERROR',
      message: 'Datos inválidos',
      details: [
        'firstName: El nombre es obligatorio',
        'phone: El teléfono es obligatorio',
      ],
    });

    expect(error.status).toBe(400);
    expect(error.error.code).toBe('VALIDATION_ERROR');
    expect(error.error.message).toBe('El nombre es obligatorio. El teléfono es obligatorio');
  });

  it('drops repeated messages', () => {
    const error = failWith(400, {
      code: 'VALIDATION_ERROR',
      message: 'Datos inválidos',
      details: ['a: La contraseña es obligatoria', 'b: La contraseña es obligatoria'],
    });

    expect(error.error.message).toBe('La contraseña es obligatoria');
  });

  it('keeps the original message when there are no details', () => {
    const error = failWith(400, { code: 'VALIDATION_ERROR', message: 'Datos inválidos' });

    expect(error.error.message).toBe('Datos inválidos');
  });

  it('does not touch other errors', () => {
    const conflict = failWith(409, { code: 'DATA_CONFLICT', message: 'Conflicto', details: ['x: y'] });
    const other = failWith(400, { code: 'INVALID_QUOTE', message: 'Presupuesto inválido' });

    expect(conflict.error.message).toBe('Conflicto');
    expect(other.error.message).toBe('Presupuesto inválido');
  });

  it('does not touch responses that are not JSON objects', () => {
    const error = failWith(500, 'Internal Server Error');

    expect(error.error).toBe('Internal Server Error');
  });
});
