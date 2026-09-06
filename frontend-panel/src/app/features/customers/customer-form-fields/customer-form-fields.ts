import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { NotificationService } from '../../../core/notifications/notification-service';
import { CustomerService } from '../customer-service';
import { CreateCustomerRequest, Customer } from '../customer.models';

@Component({
  selector: 'app-customer-form-fields',
  imports: [ReactiveFormsModule],
  templateUrl: './customer-form-fields.html',
  styleUrl: './customer-form-fields.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CustomerFormFields {
  private readonly fb = inject(FormBuilder);
  private readonly customerService = inject(CustomerService);
  private readonly notifications = inject(NotificationService);

  readonly customer = input<Customer | null>(null);
  readonly loading = input(false);
  readonly showHeader = input(true);
  readonly cancelLabel = input('Cancelar');

  readonly saved = output<Customer>();
  readonly cancelled = output<void>();

  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly isEdit = computed(() => this.customer() !== null);

  readonly form = this.fb.group({
    firstName: ['', [Validators.required, Validators.maxLength(120)]],
    lastName: ['', [Validators.required, Validators.maxLength(120)]],
    documentNumber: ['', [Validators.maxLength(30)]],
    phone: [
      '',
      [
        Validators.required,
        Validators.minLength(6),
        Validators.maxLength(30),
        Validators.pattern(/^[0-9+()\-\s]{6,30}$/),
      ],
    ],
    email: ['', [Validators.email, Validators.maxLength(255)]],
    address: ['', [Validators.maxLength(255)]],
    notes: ['', [Validators.maxLength(1000)]],
  });

  constructor() {
    effect(() => {
      const customer = this.customer();

      if (!customer) {
        return;
      }

      this.form.patchValue({
        firstName: customer.firstName,
        lastName: customer.lastName,
        documentNumber: customer.documentNumber ?? '',
        phone: customer.phone,
        email: customer.email ?? '',
        address: customer.address ?? '',
        notes: customer.notes ?? '',
      });
    });
  }

  onSubmit(): void {
    if (this.form.invalid || this.loading()) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);

    const current = this.customer();
    const request = current
      ? this.customerService.update(current.id, this.payload())
      : this.customerService.create(this.payload());

    request.subscribe({
      next: (customer) => {
        this.saving.set(false);
        this.notifications.success(
          current ? 'Cliente actualizado correctamente' : 'Cliente registrado correctamente',
        );
        this.saved.emit(customer);
      },
      error: (error: HttpErrorResponse) => {
        this.saving.set(false);
        this.errorMessage.set(
          error.error?.message ??
            (current ? 'No se pudo actualizar el cliente' : 'No se pudo registrar el cliente'),
        );
      },
    });
  }

  onCancel(): void {
    this.cancelled.emit();
  }

  private payload(): CreateCustomerRequest {
    const { firstName, lastName, documentNumber, phone, email, address, notes } =
      this.form.getRawValue();

    return {
      firstName: firstName!.trim(),
      lastName: lastName!.trim(),
      documentNumber: this.optionalValue(documentNumber),
      phone: phone!.trim(),
      email: this.optionalValue(email)?.toLowerCase() ?? null,
      address: this.optionalValue(address),
      notes: this.optionalValue(notes),
    };
  }

  private optionalValue(value: string | null): string | null {
    return value?.trim() || null;
  }
}
