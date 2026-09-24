import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { AuthService } from './auth-service';
import { LoginResponse } from './auth.models';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  const loginResponse = (mustChangePassword: boolean): LoginResponse => ({
    token: 'jwt',
    tokenType: 'Bearer',
    expiresIn: 3600,
    userId: 7,
    name: 'Ana',
    role: 'USER',
    mustChangePassword,
  });

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => localStorage.clear());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('remembers a pending password change after login', () => {
    service.login({ email: 'ana@example.com', password: 'temporal-123' }).subscribe();
    http.expectOne((req) => req.url.endsWith('/auth/login')).flush(loginResponse(true));

    expect(service.mustChangePassword()).toBe(true);
  });

  it('does not require a password change for a regular login', () => {
    service.login({ email: 'ana@example.com', password: 'clave-123' }).subscribe();
    http.expectOne((req) => req.url.endsWith('/auth/login')).flush(loginResponse(false));

    expect(service.mustChangePassword()).toBe(false);
  });

  it('clears the pending flag once the password is changed', () => {
    service.setMustChangePassword(true);

    service
      .changePassword({ currentPassword: 'temporal-123', newPassword: 'definitiva-456' })
      .subscribe();
    http.expectOne((req) => req.url.endsWith('/auth/change-password')).flush(null);

    expect(service.mustChangePassword()).toBe(false);
  });

  it('keeps the flag when the change is rejected', () => {
    service.setMustChangePassword(true);

    service
      .changePassword({ currentPassword: 'mala', newPassword: 'definitiva-456' })
      .subscribe({ error: () => undefined });
    http
      .expectOne((req) => req.url.endsWith('/auth/change-password'))
      .flush({ code: 'INVALID_CURRENT_PASSWORD' }, { status: 401, statusText: 'Unauthorized' });

    expect(service.mustChangePassword()).toBe(true);
  });

  it('forgets the pending flag on logout', () => {
    service.setMustChangePassword(true);

    service.logout();

    expect(service.mustChangePassword()).toBe(false);
  });
});
