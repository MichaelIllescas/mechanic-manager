import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { AuthService } from '../../../core/auth/auth-service';
import { ChangePasswordRequired } from './change-password-required';

describe('ChangePasswordRequired', () => {
  let component: ChangePasswordRequired;
  let fixture: ComponentFixture<ChangePasswordRequired>;
  let router: Router;

  const auth = {
    changePassword: vi.fn(),
    isAdmin: vi.fn(),
    logout: vi.fn(),
  };

  beforeEach(async () => {
    auth.changePassword.mockReset().mockReturnValue(of(undefined));
    auth.isAdmin.mockReset().mockReturnValue(false);
    auth.logout.mockReset();

    await TestBed.configureTestingModule({
      imports: [ChangePasswordRequired],
      providers: [provideRouter([]), { provide: AuthService, useValue: auth }],
    }).compileComponents();

    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);

    fixture = TestBed.createComponent(ChangePasswordRequired);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('does not submit an invalid form', () => {
    component.onSubmit();

    expect(auth.changePassword).not.toHaveBeenCalled();
  });

  it('rejects keeping the temporary password', () => {
    component.form.setValue({
      currentPassword: 'temporal-123',
      newPassword: 'temporal-123',
      confirmation: 'temporal-123',
    });

    expect(component.form.hasError('passwordNotChanged')).toBe(true);
  });

  it('changes the password and goes to the dashboard', () => {
    component.form.setValue({
      currentPassword: 'temporal-123',
      newPassword: 'definitiva-456',
      confirmation: 'definitiva-456',
    });

    component.onSubmit();

    expect(auth.changePassword).toHaveBeenCalledWith({
      currentPassword: 'temporal-123',
      newPassword: 'definitiva-456',
    });
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
  });

  it('logs out and returns to login', () => {
    component.logout();

    expect(auth.logout).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});
