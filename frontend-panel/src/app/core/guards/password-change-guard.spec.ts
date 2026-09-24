import { TestBed } from '@angular/core/testing';
import { Router, UrlTree, provideRouter } from '@angular/router';

import { AuthService } from '../auth/auth-service';
import { passwordChangeGuard } from './password-change-guard';

describe('passwordChangeGuard', () => {
  const auth = {
    isAuthenticated: vi.fn(),
    mustChangePassword: vi.fn(),
    isAdmin: vi.fn(),
  };

  const run = () =>
    TestBed.runInInjectionContext(() => passwordChangeGuard({} as never, [])) as boolean | UrlTree;

  const urlOf = (result: boolean | UrlTree) =>
    TestBed.inject(Router).serializeUrl(result as UrlTree);

  beforeEach(() => {
    auth.isAuthenticated.mockReset().mockReturnValue(true);
    auth.mustChangePassword.mockReset().mockReturnValue(true);
    auth.isAdmin.mockReset().mockReturnValue(false);

    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: AuthService, useValue: auth }],
    });
  });

  it('lets in an account with a pending password change', () => {
    expect(run()).toBe(true);
  });

  it('sends anonymous visitors to login', () => {
    auth.isAuthenticated.mockReturnValue(false);

    expect(urlOf(run())).toBe('/login');
  });

  it('sends an account without pending change to its home', () => {
    auth.mustChangePassword.mockReturnValue(false);

    expect(urlOf(run())).toBe('/dashboard');

    auth.isAdmin.mockReturnValue(true);

    expect(urlOf(run())).toBe('/admin/usuarios');
  });
});
