import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { NotificationService } from '../../../../../core/notifications/notification-service';
import { ServiceTypeService } from '../../../../service-types/service-type-service';
import {
  CreateServiceTypeRequest,
  ServiceType,
} from '../../../../service-types/service-type.models';

@Component({
  selector: 'app-service-quick-create',
  imports: [ReactiveFormsModule],
  templateUrl: './service-quick-create.html',
  styleUrl: '../quick-create.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ServiceQuickCreate {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ServiceTypeService);
  private readonly notifications = inject(NotificationService);
  readonly saved = output<ServiceType>();
  readonly cancelled = output<void>();
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly form = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(120)]],
    description: ['', Validators.maxLength(1000)],
    basePrice: [null as number | null, [Validators.required, Validators.min(0)]],
    estimatedMinutes: [null as number | null, Validators.min(1)],
  });

  save(): void {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.errorMessage.set(null);
    this.service.create(this.payload()).subscribe({
      next: (created) => {
        this.saving.set(false);
        this.notifications.success('Servicio registrado correctamente');
        this.saved.emit(created);
      },
      error: (error: HttpErrorResponse) => {
        this.saving.set(false);
        this.errorMessage.set(error.error?.message ?? 'No se pudo registrar el servicio');
      },
    });
  }

  cancel(): void {
    if (!this.saving()) this.cancelled.emit();
  }

  private payload(): CreateServiceTypeRequest {
    const value = this.form.getRawValue();
    return {
      name: value.name!.trim(),
      description: value.description?.trim() || null,
      basePrice: Number(value.basePrice),
      estimatedMinutes: value.estimatedMinutes,
    };
  }
}
