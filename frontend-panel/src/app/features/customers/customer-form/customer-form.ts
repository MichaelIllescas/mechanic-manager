import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { CustomerFormFields } from '../customer-form-fields/customer-form-fields';
import { CustomerService } from '../customer-service';
import { Customer } from '../customer.models';

@Component({
  selector: 'app-customer-form',
  imports: [CustomerFormFields, RouterLink],
  templateUrl: './customer-form.html',
  styleUrl: './customer-form.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CustomerForm implements OnInit {
  private readonly customerService = inject(CustomerService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly isEdit = signal(false);
  readonly loadingCustomer = signal(false);
  readonly customer = signal<Customer | null>(null);
  readonly errorMessage = signal<string | null>(null);

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

    this.isEdit.set(true);
    this.loadingCustomer.set(true);

    this.customerService.findFromListById(customerId).subscribe({
      next: (customer) => {
        this.loadingCustomer.set(false);

        if (!customer) {
          this.errorMessage.set('No se encontró el cliente solicitado');
          return;
        }

        this.customer.set(customer);
      },
      error: (error: HttpErrorResponse) => {
        this.loadingCustomer.set(false);
        this.errorMessage.set(error.error?.message ?? 'No se pudo cargar el cliente');
      },
    });
  }

  onSaved(): void {
    void this.router.navigate(['/customers']);
  }

  onCancelled(): void {
    void this.router.navigate(['/customers']);
  }
}
