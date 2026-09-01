import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { NotificationService } from '../../../core/notifications/notification-service';
import { CustomerService } from '../../customers/customer-service';
import { Customer } from '../../customers/customer.models';
import { VehicleService } from '../vehicle-service';
import { CreateVehicleRequest, VehicleType } from '../vehicle.models';

@Component({
  selector: 'app-vehicle-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './vehicle-form.html',
  styleUrl: './vehicle-form.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VehicleForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly vehicleService = inject(VehicleService);
  private readonly customerService = inject(CustomerService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly notifications = inject(NotificationService);

  private vehicleId: number | null = null;

  readonly isEdit = signal(false);
  readonly loadingCustomers = signal(false);
  readonly loadingVehicle = signal(false);
  readonly loading = computed(() => this.loadingCustomers() || this.loadingVehicle());
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly customers = signal<Customer[]>([]);
  readonly customerSearch = signal('');
  readonly customerResultsOpen = signal(false);
  readonly selectedCustomerLabel = signal<string | null>(null);
  readonly filteredCustomers = computed(() => {
    const search = this.normalizeSearch(this.customerSearch());
    const customers = this.customers();

    if (!search) {
      return [];
    }

    return customers
      .filter((customer) => this.customerMatchesSearch(customer, search))
      .slice(0, 8);
  });

  readonly vehicleTypes: ReadonlyArray<{ value: VehicleType; label: string }> = [
    { value: 'CAR', label: 'Auto' },
    { value: 'SUV', label: 'SUV' },
    { value: 'PICKUP', label: 'Pickup' },
    { value: 'VAN', label: 'Utilitario' },
    { value: 'TRUCK', label: 'Camión' },
    { value: 'MOTORCYCLE', label: 'Motocicleta' },
    { value: 'OTHER', label: 'Otro' },
  ];

  readonly form = this.fb.group({
    customerId: [null as number | null, [Validators.required, Validators.min(1)]],
    licensePlate: ['', [Validators.required, Validators.maxLength(20)]],
    brand: ['', [Validators.required, Validators.maxLength(80)]],
    model: ['', [Validators.required, Validators.maxLength(120)]],
    year: [null as number | null, [Validators.min(1886), Validators.max(9999)]],
    chassisNumber: ['', [Validators.maxLength(100)]],
    engineNumber: ['', [Validators.maxLength(100)]],
    mileage: [null as number | null, [Validators.min(0)]],
    vehicleType: ['CAR' as VehicleType, [Validators.required]],
    notes: ['', [Validators.maxLength(1000)]],
  });

  ngOnInit(): void {
    this.loadCustomers();

    const routeId = this.route.snapshot.paramMap.get('id');
    if (routeId === null) {
      return;
    }

    const vehicleId = Number(routeId);
    if (!Number.isInteger(vehicleId) || vehicleId <= 0) {
      this.errorMessage.set('El identificador del vehículo no es válido');
      return;
    }

    this.vehicleId = vehicleId;
    this.isEdit.set(true);
    this.loadVehicle(vehicleId);
  }

  onSubmit(): void {
    if (this.form.invalid || this.loading()) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);

    const request = this.isEdit()
      ? this.vehicleService.update(this.vehicleId!, this.payload())
      : this.vehicleService.create(this.payload());

    request.subscribe({
      next: () => {
        this.notifications.success(
          this.isEdit()
            ? 'Vehículo actualizado correctamente'
            : 'Vehículo registrado correctamente',
        );
        void this.router.navigate(['/vehicles']);
      },
      error: (error: HttpErrorResponse) => {
        this.saving.set(false);
        this.errorMessage.set(
          error.error?.message ??
            (this.isEdit()
              ? 'No se pudo actualizar el vehículo'
              : 'No se pudo registrar el vehículo'),
        );
      },
    });
  }

  onCustomerSearch(value: string): void {
    this.customerSearch.set(value);
    this.customerResultsOpen.set(true);
  }

  openCustomerResults(): void {
    if (!this.selectedCustomerLabel() && this.customerSearch().trim()) {
      this.customerResultsOpen.set(true);
    }
  }

  selectCustomer(customer: Customer): void {
    this.form.controls.customerId.setValue(customer.id);
    this.form.controls.customerId.markAsTouched();
    this.selectedCustomerLabel.set(this.formatCustomer(customer));
    this.customerSearch.set('');
    this.customerResultsOpen.set(false);
  }

  changeCustomer(): void {
    this.form.controls.customerId.setValue(null);
    this.form.controls.customerId.markAsTouched();
    this.selectedCustomerLabel.set(null);
    this.customerSearch.set('');
    this.customerResultsOpen.set(false);
  }

  private loadCustomers(): void {
    this.loadingCustomers.set(true);

    this.customerService.findAll().subscribe({
      next: (customers) => {
        this.customers.set(customers);
        this.syncSelectedCustomerFromList();
        this.loadingCustomers.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.loadingCustomers.set(false);
        this.errorMessage.set(error.error?.message ?? 'No se pudieron cargar los clientes');
      },
    });
  }

  private loadVehicle(vehicleId: number): void {
    this.loadingVehicle.set(true);

    this.vehicleService.findFromListById(vehicleId).subscribe({
      next: (vehicle) => {
        this.loadingVehicle.set(false);

        if (!vehicle) {
          this.errorMessage.set('No se encontró el vehículo solicitado');
          return;
        }

        this.form.patchValue({
          customerId: vehicle.customerId,
          licensePlate: vehicle.licensePlate,
          brand: vehicle.brand,
          model: vehicle.model,
          year: vehicle.year,
          chassisNumber: vehicle.chassisNumber ?? '',
          engineNumber: vehicle.engineNumber ?? '',
          mileage: vehicle.mileage,
          vehicleType: vehicle.vehicleType,
          notes: vehicle.notes ?? '',
        });
        this.selectedCustomerLabel.set(
          this.formatCustomerName(vehicle.customerFirstName, vehicle.customerLastName),
        );
        this.syncSelectedCustomerFromList();
      },
      error: (error: HttpErrorResponse) => {
        this.loadingVehicle.set(false);
        this.errorMessage.set(error.error?.message ?? 'No se pudo cargar el vehículo');
      },
    });
  }

  private payload(): CreateVehicleRequest {
    const {
      customerId,
      licensePlate,
      brand,
      model,
      year,
      chassisNumber,
      engineNumber,
      mileage,
      vehicleType,
      notes,
    } = this.form.getRawValue();

    return {
      customerId: customerId!,
      licensePlate: licensePlate!.trim().toUpperCase(),
      brand: brand!.trim(),
      model: model!.trim(),
      year,
      chassisNumber: this.trimToNull(chassisNumber),
      engineNumber: this.trimToNull(engineNumber),
      mileage,
      vehicleType: vehicleType!,
      notes: notes?.trim() || null,
    };
  }

  private trimToNull(value: string | null | undefined): string | null {
    return value?.trim() || null;
  }

  private syncSelectedCustomerFromList(): void {
    const customerId = this.form.controls.customerId.value;

    if (!customerId) {
      return;
    }

    const customer = this.customers().find((customer) => customer.id === customerId);

    if (customer) {
      this.selectedCustomerLabel.set(this.formatCustomer(customer));
    }
  }

  private customerMatchesSearch(customer: Customer, search: string): boolean {
    return this.normalizeSearch(
      [
        customer.firstName,
        customer.lastName,
        customer.documentNumber,
        customer.phone,
        customer.email,
      ]
        .filter(Boolean)
        .join(' '),
    ).includes(search);
  }

  private formatCustomer(customer: Customer): string {
    const details = [customer.documentNumber, customer.phone].filter(Boolean).join(' · ');
    const name = this.formatCustomerName(customer.firstName, customer.lastName);

    return details ? `${name} · ${details}` : name;
  }

  private formatCustomerName(firstName: string, lastName: string): string {
    return `${firstName} ${lastName}`;
  }

  private normalizeSearch(value: string): string {
    return value.trim().toLowerCase();
  }
}
