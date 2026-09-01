import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { CustomerService } from '../customer-service';
import { Customer } from '../customer.models';

@Component({
  selector: 'app-customer-detail',
  imports: [RouterLink],
  templateUrl: './customer-detail.html',
  styleUrl: './customer-detail.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CustomerDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly customerService = inject(CustomerService);

  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly customer = signal<Customer | null>(null);

  ngOnInit(): void {
    const routeId = this.route.snapshot.paramMap.get('id');
    const customerId = Number(routeId);

    if (!Number.isInteger(customerId) || customerId <= 0) {
      this.errorMessage.set('El identificador del cliente no es válido');
      return;
    }

    this.loadCustomer(customerId);
  }

  displayValue(value: string | null): string {
    return value === null || value === '' ? 'Sin informar' : value;
  }

  displayDateTime(value: string | null): string {
    if (!value) {
      return 'Sin informar';
    }

    return new Intl.DateTimeFormat('es-AR', {
      dateStyle: 'short',
      timeStyle: 'short',
    }).format(new Date(value));
  }

  initials(customer: Customer): string {
    return `${customer.firstName.charAt(0)}${customer.lastName.charAt(0)}`.toUpperCase();
  }

  private loadCustomer(customerId: number): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.customerService.findFromListById(customerId).subscribe({
      next: (customer) => {
        this.loading.set(false);

        if (!customer) {
          this.errorMessage.set('No se encontró el cliente solicitado');
          return;
        }

        this.customer.set(customer);
      },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);
        this.errorMessage.set(error.error?.message ?? 'No se pudo cargar el cliente');
      },
    });
  }
}
