import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { NotificationService } from '../../../../../core/notifications/notification-service';
import { SupplyService } from '../../../../supplies/supply-service';
import { CreateSupplyRequest, Supply } from '../../../../supplies/supply.models';

@Component({
  selector: 'app-supply-quick-create',
  imports: [ReactiveFormsModule],
  templateUrl: './supply-quick-create.html',
  styleUrl: '../quick-create.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SupplyQuickCreate {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(SupplyService);
  private readonly notifications = inject(NotificationService);
  readonly saved = output<Supply>();
  readonly cancelled = output<void>();
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly form = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(120)]],
    price: [null as number | null, [Validators.required, Validators.min(0)]],
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
        this.notifications.success('Insumo registrado correctamente');
        this.saved.emit(created);
      },
      error: (error: HttpErrorResponse) => {
        this.saving.set(false);
        this.errorMessage.set(error.error?.message ?? 'No se pudo registrar el insumo');
      },
    });
  }

  cancel(): void {
    if (!this.saving()) this.cancelled.emit();
  }

  private payload(): CreateSupplyRequest {
    const value = this.form.getRawValue();
    return { name: value.name!.trim(), price: Number(value.price) };
  }
}
