import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { NotificationService } from '../../../../../core/notifications/notification-service';
import { CustomerService } from '../../../../customers/customer-service';
import { CreateCustomerRequest, Customer } from '../../../../customers/customer.models';

@Component({
  selector: 'app-customer-quick-create',
  imports: [ReactiveFormsModule],
  templateUrl: './customer-quick-create.html',
  styleUrl: '../quick-create.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CustomerQuickCreate {
  private readonly fb = inject(FormBuilder);
  private readonly customerService = inject(CustomerService);
  private readonly notifications = inject(NotificationService);

  readonly saved = output<Customer>();
  readonly cancelled = output<void>();
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.group({
    firstName: ['', [Validators.required, Validators.maxLength(120)]],
    lastName: ['', [Validators.required, Validators.maxLength(120)]],
    documentNumber: ['', [Validators.maxLength(30)]],
    phone: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(30)]],
    email: ['', [Validators.email, Validators.maxLength(255)]],
    address: ['', [Validators.maxLength(255)]],
    notes: ['', [Validators.maxLength(1000)]],
  });

  save(): void {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);

    this.customerService.create(this.payload()).subscribe({
      next: (customer) => {
        this.saving.set(false);
        this.notifications.success('Cliente registrado correctamente');
        this.saved.emit(customer);
      },
      error: (error: HttpErrorResponse) => {
        this.saving.set(false);
        this.errorMessage.set(error.error?.message ?? 'No se pudo registrar el cliente');
      },
    });
  }

  cancel(): void {
    if (!this.saving()) {
      this.cancelled.emit();
    }
  }

  private payload(): CreateCustomerRequest {
    const value = this.form.getRawValue();

    return {
      firstName: value.firstName!.trim(),
      lastName: value.lastName!.trim(),
      documentNumber: this.optionalValue(value.documentNumber),
      phone: value.phone!.trim(),
      email: this.optionalValue(value.email)?.toLowerCase() ?? null,
      address: this.optionalValue(value.address),
      notes: this.optionalValue(value.notes),
    };
  }

  private optionalValue(value: string | null | undefined): string | null {
    return value?.trim() || null;
  }
}
