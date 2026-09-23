import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  computed,
  inject,
  output,
  signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { NotificationService } from '../../../../core/notifications/notification-service';
import { Modal } from '../../../../shared/components/modal/modal';
import { CustomerService } from '../../../customers/customer-service';
import { Customer } from '../../../customers/customer.models';
import { VehicleService } from '../../../vehicles/vehicle-service';
import { Vehicle, VehicleType } from '../../../vehicles/vehicle.models';

@Component({
  selector: 'app-vehicle-quick-create',
  imports: [Modal, ReactiveFormsModule],
  templateUrl: './vehicle-quick-create.html',
  styleUrl: './vehicle-quick-create.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VehicleQuickCreate implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly customerService = inject(CustomerService);
  private readonly vehicleService = inject(VehicleService);
  private readonly notifications = inject(NotificationService);

  readonly saved = output<Vehicle>();
  readonly cancelled = output<void>();

  readonly customers = signal<Customer[]>([]);
  readonly loadingCustomers = signal(false);
  readonly customerSearch = signal('');
  readonly customerResultsOpen = signal(false);
  readonly selectedCustomer = signal<Customer | null>(null);
  readonly creatingCustomer = signal(false);
  readonly savingCustomer = signal(false);
  readonly savingVehicle = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly filteredCustomers = computed(() => {
    const search = this.customerSearch().trim().toLowerCase();
    if (!search) {
      return [];
    }

    return this.customers()
      .filter((customer) =>
        [customer.firstName, customer.lastName, customer.documentNumber, customer.phone]
          .filter(Boolean)
          .join(' ')
          .toLowerCase()
          .includes(search),
      )
      .slice(0, 6);
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

  readonly vehicleForm = this.fb.group({
    licensePlate: ['', [Validators.required, Validators.maxLength(20)]],
    vehicleType: ['CAR' as VehicleType, Validators.required],
    brand: ['', [Validators.required, Validators.maxLength(80)]],
    model: ['', [Validators.required, Validators.maxLength(120)]],
  });

  readonly customerForm = this.fb.group({
    firstName: ['', [Validators.required, Validators.maxLength(120)]],
    lastName: ['', [Validators.required, Validators.maxLength(120)]],
    phone: [
      '',
      [
        Validators.required,
        Validators.minLength(6),
        Validators.maxLength(30),
        Validators.pattern(/^[0-9+()\-\s]{6,30}$/),
      ],
    ],
  });

  ngOnInit(): void {
    this.loadCustomers();
  }

  onCustomerSearch(value: string): void {
    this.customerSearch.set(value);
    this.customerResultsOpen.set(true);
  }

  selectCustomer(customer: Customer): void {
    this.selectedCustomer.set(customer);
    this.customerSearch.set('');
    this.customerResultsOpen.set(false);
    this.errorMessage.set(null);
  }

  changeCustomer(): void {
    this.selectedCustomer.set(null);
    this.customerSearch.set('');
    this.customerResultsOpen.set(false);
  }

  startCustomerCreation(): void {
    this.customerResultsOpen.set(false);
    this.creatingCustomer.set(true);
    this.errorMessage.set(null);
  }

  cancelCustomerCreation(): void {
    if (!this.savingCustomer()) {
      this.creatingCustomer.set(false);
      this.customerForm.reset();
    }
  }

  saveCustomer(): void {
    if (this.customerForm.invalid || this.savingCustomer()) {
      this.customerForm.markAllAsTouched();
      return;
    }

    this.savingCustomer.set(true);
    this.errorMessage.set(null);
    const value = this.customerForm.getRawValue();

    this.customerService
      .create({
        firstName: value.firstName!.trim(),
        lastName: value.lastName!.trim(),
        phone: value.phone!.trim(),
        documentNumber: null,
        email: null,
        address: null,
        notes: null,
      })
      .subscribe({
        next: (customer) => {
          this.customers.update((current) => [...current, customer]);
          this.selectCustomer(customer);
          this.creatingCustomer.set(false);
          this.savingCustomer.set(false);
          this.notifications.success('Cliente registrado correctamente');
        },
        error: (error: HttpErrorResponse) => {
          this.savingCustomer.set(false);
          this.errorMessage.set(error.error?.message ?? 'No se pudo registrar el cliente');
        },
      });
  }

  saveVehicle(): void {
    const customer = this.selectedCustomer();
    if (!customer || this.vehicleForm.invalid || this.savingVehicle()) {
      this.vehicleForm.markAllAsTouched();
      if (!customer) {
        this.errorMessage.set('Seleccioná o registrá un cliente para el vehículo');
      }
      return;
    }

    this.savingVehicle.set(true);
    this.errorMessage.set(null);
    const value = this.vehicleForm.getRawValue();

    this.vehicleService
      .create({
        customerId: customer.id,
        licensePlate: value.licensePlate!.trim().toUpperCase(),
        brand: value.brand!.trim(),
        model: value.model!.trim(),
        vehicleType: value.vehicleType!,
        year: null,
        chassisNumber: null,
        engineNumber: null,
        mileage: null,
        notes: null,
      })
      .subscribe({
        next: (vehicle) => {
          this.savingVehicle.set(false);
          this.notifications.success('Vehículo registrado correctamente');
          this.saved.emit(vehicle);
        },
        error: (error: HttpErrorResponse) => {
          this.savingVehicle.set(false);
          this.errorMessage.set(error.error?.message ?? 'No se pudo registrar el vehículo');
        },
      });
  }

  cancel(): void {
    if (!this.savingCustomer() && !this.savingVehicle()) {
      this.cancelled.emit();
    }
  }

  private loadCustomers(): void {
    this.loadingCustomers.set(true);
    this.customerService.findAll().subscribe({
      next: (customers) => {
        this.customers.set(customers);
        this.loadingCustomers.set(false);
        if (customers.length === 0) {
          this.creatingCustomer.set(true);
        }
      },
      error: (error: HttpErrorResponse) => {
        this.loadingCustomers.set(false);
        this.errorMessage.set(error.error?.message ?? 'No se pudieron cargar los clientes');
      },
    });
  }
}
