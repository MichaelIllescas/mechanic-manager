import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { NotificationService } from '../../../core/notifications/notification-service';
import { ServiceTypeService } from '../service-type-service';

@Component({
  selector: 'app-service-type-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './service-type-form.html',
  styleUrl: './service-type-form.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ServiceTypeForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly serviceTypeService = inject(ServiceTypeService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly notifications = inject(NotificationService);

  private serviceTypeId: number | null = null;

  readonly isEdit = signal(false);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(120)]],
    description: ['', [Validators.maxLength(500)]],
    basePrice: [null as number | null, [Validators.required, Validators.min(0)]],
    estimatedMinutes: [null as number | null, [Validators.min(1)]],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id === null) {
      return;
    }

    this.serviceTypeId = Number(id);
    this.isEdit.set(true);

    this.serviceTypeService.findById(this.serviceTypeId).subscribe({
      next: (serviceType) =>
        this.form.patchValue({
          name: serviceType.name,
          description: serviceType.description ?? '',
          basePrice: serviceType.basePrice,
          estimatedMinutes: serviceType.estimatedMinutes,
        }),
      error: (err: HttpErrorResponse) =>
        this.errorMessage.set(err.error?.message ?? 'No se pudo cargar el servicio'),
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    const payload = this.payload();

    const request = this.isEdit()
      ? this.serviceTypeService.update(this.serviceTypeId!, payload)
      : this.serviceTypeService.create(payload);

    request.subscribe({
      next: () => {
        this.notifications.success(
          this.isEdit() ? 'Servicio actualizado correctamente' : 'Servicio creado correctamente',
        );
        this.router.navigate(['/servicios']);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.errorMessage.set(
          err.error?.message ??
            (this.isEdit() ? 'No se pudo actualizar el servicio' : 'No se pudo crear el servicio'),
        );
      },
    });
  }

  /**
   * El backend espera null en los campos opcionales vacios, pero un input de
   * texto vacio devuelve '' y no null. Sin esta conversion se guardarian
   * descripciones vacias en lugar de ausentes.
   */
  private payload() {
    const { name, description, basePrice, estimatedMinutes } = this.form.getRawValue();

    return {
      name: name!.trim(),
      description: description?.trim() ? description.trim() : null,
      basePrice: basePrice!,
      estimatedMinutes: estimatedMinutes ?? null,
    };
  }
}
