import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { NotificationService } from '../../../../core/notifications/notification-service';
import { UserService } from '../user-service';

@Component({
  selector: 'app-user-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './user-form.html',
  styleUrl: './user-form.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly userService = inject(UserService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly notifications = inject(NotificationService);

  private userId: number | null = null;

  readonly isEdit = signal(false);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
    role: ['USER' as 'SYSTEM_ADMIN' | 'USER', [Validators.required]],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id === null) {
      return;
    }

    this.userId = Number(id);
    this.isEdit.set(true);

    // En edicion la contrasena no se envia: el backend no la acepta en el PUT.
    this.form.controls.password.disable();

    this.userService.findById(this.userId).subscribe({
      next: (user) =>
        this.form.patchValue({
          name: user.name,
          email: user.email,
          role: user.role,
        }),
      error: (err: HttpErrorResponse) =>
        this.errorMessage.set(err.error?.message ?? 'No se pudo cargar el usuario'),
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    const request = this.isEdit() ? this.updateRequest() : this.createRequest();

    request.subscribe({
      next: () => {
        this.notifications.success(
          this.isEdit() ? 'Usuario actualizado correctamente' : 'Usuario creado correctamente',
        );
        this.router.navigate(['/admin/usuarios']);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.errorMessage.set(
          err.error?.message ??
            (this.isEdit() ? 'No se pudo actualizar el usuario' : 'No se pudo crear el usuario'),
        );
      },
    });
  }

  private createRequest() {
    return this.userService.create(this.form.getRawValue());
  }

  private updateRequest() {
    const { name, email, role } = this.form.getRawValue();

    return this.userService.update(this.userId!, { name, email, role });
  }
}
