import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { NotificationService } from '../../../core/notifications/notification-service';
import { CustomerService } from '../customer-service';
import { CreateCustomerRequest } from '../customer.models';

@Component({
  selector: 'app-customer-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './customer-form.html',
  styleUrl: './customer-form.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CustomerForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly customerService = inject(CustomerService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly notifications = inject(NotificationService);

  private customerId: number | null = null;

  readonly isEdit = signal(false);
  readonly loadingCustomer = signal(false);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);

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
    notes: ['', [Validators.maxLength(1000)]],
  });

  ngOnInit(): void {
    const routeId = this.route.snapshot.paramMap.get('id');

    if (routeId === null) {
      return;
    }

    const customerId = Number(routeId);
    if (!Number.isInteger(customerId) || customerId <= 0) {
      this.errorMessage.set('El identificador del cliente no es válido');
      return;
    }

    this.customerId = customerId;
    this.isEdit.set(true);
    this.loadingCustomer.set(true);

    this.customerService.findFromListById(customerId).subscribe({
      next: (customer) => {
        this.loadingCustomer.set(false);

        if (!customer) {
          this.errorMessage.set('No se encontró el cliente solicitado');
          return;
        }

        this.form.patchValue({
          firstName: customer.firstName,
          lastName: customer.lastName,
          documentNumber: customer.documentNumber ?? '',
          phone: customer.phone,
          email: customer.email ?? '',
          notes: customer.notes ?? '',
        });
      },
      error: (error: HttpErrorResponse) => {
        this.loadingCustomer.set(false);
        this.errorMessage.set(error.error?.message ?? 'No se pudo cargar el cliente');
      },
    });
  }

  onSubmit(): void {
    if (this.form.invalid || this.loadingCustomer()) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);

    const request = this.isEdit()
      ? this.customerService.update(this.customerId!, this.payload())
      : this.customerService.create(this.payload());

    request.subscribe({
      next: () => {
        this.notifications.success(
          this.isEdit() ? 'Cliente actualizado correctamente' : 'Cliente registrado correctamente',
        );
        void this.router.navigate(['/customers']);
      },
      error: (error: HttpErrorResponse) => {
        this.saving.set(false);
        this.errorMessage.set(
          error.error?.message ??
            (this.isEdit()
              ? 'No se pudo actualizar el cliente'
              : 'No se pudo registrar el cliente'),
        );
      },
    });
  }

  private payload(): CreateCustomerRequest {
    const { firstName, lastName, documentNumber, phone, email, notes } = this.form.getRawValue();

    return {
      firstName: firstName!.trim(),
      lastName: lastName!.trim(),
      documentNumber: this.optionalValue(documentNumber),
      phone: phone!.trim(),
      email: this.optionalValue(email)?.toLowerCase() ?? null,
      notes: this.optionalValue(notes),
    };
  }

  private optionalValue(value: string | null): string | null {
    return value?.trim() || null;
  }
}
