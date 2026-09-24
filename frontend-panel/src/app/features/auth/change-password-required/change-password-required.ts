import { Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

import { AuthService } from '../../../core/auth/auth-service';
import { NotificationService } from '../../../core/notifications/notification-service';

function passwordsMatch(group: AbstractControl): ValidationErrors | null {
  const password = group.get('newPassword')?.value;
  const confirmation = group.get('confirmation')?.value;

  return password === confirmation ? null : { passwordsMismatch: true };
}

function passwordIsDifferent(group: AbstractControl): ValidationErrors | null {
  const current = group.get('currentPassword')?.value;
  const password = group.get('newPassword')?.value;

  return current && current === password ? { passwordNotChanged: true } : null;
}

@Component({
  selector: 'app-change-password-required',
  imports: [ReactiveFormsModule],
  templateUrl: './change-password-required.html',
  styleUrl: '../login/login.css',
  styles: `
    button.login-form__link {
      background: none;
      border: 0;
      cursor: pointer;
      font-family: inherit;
    }
  `,
})
export class ChangePasswordRequired {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly notifications = inject(NotificationService);

  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly passwordVisible = signal(false);

  readonly form = this.fb.nonNullable.group(
    {
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
      confirmation: ['', [Validators.required]],
    },
    { validators: [passwordsMatch, passwordIsDifferent] },
  );

  togglePasswordVisibility(): void {
    this.passwordVisible.update((visible) => !visible);
  }

  logout(): void {
    this.auth.logout();
    void this.router.navigate(['/login']);
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    const { currentPassword, newPassword } = this.form.getRawValue();

    this.auth.changePassword({ currentPassword, newPassword }).subscribe({
      next: () => {
        this.loading.set(false);
        this.notifications.success('Tu contraseña se actualizó correctamente');
        void this.router.navigate([this.auth.isAdmin() ? '/admin/usuarios' : '/dashboard']);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.errorMessage.set(err.error?.message ?? 'No se pudo conectar con el servidor');
      },
    });
  }
}
