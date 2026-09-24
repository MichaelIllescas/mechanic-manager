import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { AuthService } from '../../core/auth/auth-service';
import { NotificationService } from '../../core/notifications/notification-service';
import { Account } from './account';

describe('Account', () => {
  let fixture: ComponentFixture<Account>;
  let component: Account;

  const auth = {
    getUserName: vi.fn().mockReturnValue('Orlando'),
    isAdmin: vi.fn().mockReturnValue(false),
    changePassword: vi.fn().mockReturnValue(of(undefined)),
  };
  const notifications = { success: vi.fn() };

  beforeEach(async () => {
    auth.changePassword.mockReset().mockReturnValue(of(undefined));
    notifications.success.mockReset();

    await TestBed.configureTestingModule({
      imports: [Account],
      providers: [
        { provide: AuthService, useValue: auth },
        { provide: NotificationService, useValue: notifications },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Account);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('shows the password requirements as pending initially', () => {
    const requirements = fixture.nativeElement.querySelectorAll('.account__requirements li');

    expect(requirements).toHaveLength(3);
    expect(fixture.nativeElement.querySelectorAll('.account__requirement--valid')).toHaveLength(0);
  });

  it('marks every requirement as completed for a valid password change', () => {
    component.form.setValue({
      currentPassword: 'actual-123',
      newPassword: 'nueva-segura-456',
      confirmation: 'nueva-segura-456',
    });
    fixture.detectChanges();

    expect(component.hasValidLength()).toBe(true);
    expect(component.hasDifferentPassword()).toBe(true);
    expect(component.hasMatchingConfirmation()).toBe(true);
    expect(fixture.nativeElement.querySelectorAll('.account__requirement--valid')).toHaveLength(3);
  });

  it('submits a valid password change and clears the form', () => {
    component.form.setValue({
      currentPassword: 'actual-123',
      newPassword: 'nueva-segura-456',
      confirmation: 'nueva-segura-456',
    });

    component.onSubmit();

    expect(auth.changePassword).toHaveBeenCalledWith({
      currentPassword: 'actual-123',
      newPassword: 'nueva-segura-456',
    });
    expect(notifications.success).toHaveBeenCalledWith(
      'Tu contraseña se actualizó correctamente',
    );
    expect(component.form.getRawValue()).toEqual({
      currentPassword: '',
      newPassword: '',
      confirmation: '',
    });
  });
});
