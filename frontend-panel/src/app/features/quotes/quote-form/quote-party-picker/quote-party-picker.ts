import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  effect,
  inject,
  input,
  signal,
  WritableSignal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import {
  catchError,
  debounceTime,
  distinctUntilChanged,
  of,
  startWith,
  switchMap,
  tap,
} from 'rxjs';

import { CustomerService } from '../../../customers/customer-service';
import { Customer } from '../../../customers/customer.models';
import { VehicleService } from '../../../vehicles/vehicle-service';
import { Vehicle } from '../../../vehicles/vehicle.models';
import { CustomerQuickCreate } from '../quick-create/customer-quick-create/customer-quick-create';
import { VehicleQuickCreate } from '../quick-create/vehicle-quick-create/vehicle-quick-create';

@Component({
  selector: 'app-quote-party-picker',
  imports: [ReactiveFormsModule, CustomerQuickCreate, VehicleQuickCreate],
  templateUrl: './quote-party-picker.html',
  styleUrl: './quote-party-picker.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class QuotePartyPicker {
  private readonly customersService = inject(CustomerService);
  private readonly vehiclesService = inject(VehicleService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly allVehicleResults = signal<Vehicle[]>([]);

  readonly form = input.required<FormGroup>();
  readonly initialCustomerLabel = input('');
  readonly initialVehicleLabel = input('');
  readonly customerSearch = new FormControl('', { nonNullable: true });
  readonly vehicleSearch = new FormControl('', { nonNullable: true });
  readonly customers = signal<Customer[]>([]);
  readonly vehicles = signal<Vehicle[]>([]);
  readonly customerLabel = signal('');
  readonly vehicleLabel = signal('');
  readonly customerResultsOpen = signal(false);
  readonly vehicleResultsOpen = signal(false);
  readonly loadingCustomers = signal(false);
  readonly loadingVehicles = signal(false);
  readonly creationMode = signal<'customer' | 'vehicle' | null>(null);
  readonly errorMessage = signal<string | null>(null);

  constructor() {
    effect(() => {
      if (this.initialCustomerLabel()) this.customerLabel.set(this.initialCustomerLabel());
      if (this.initialVehicleLabel()) this.vehicleLabel.set(this.initialVehicleLabel());
    });
    this.configureSearches();
  }

  customerId(): number | null {
    return this.form().get('customerId')?.value ?? null;
  }
  vehicleId(): number | null {
    return this.form().get('vehicleId')?.value ?? null;
  }

  selectCustomer(customer: Customer): void {
    this.form().get('customerId')?.setValue(customer.id);
    this.form().get('customerId')?.markAsTouched();
    this.customerLabel.set(this.formatCustomer(customer));
    this.customerResultsOpen.set(false);
    this.clearVehicle();
    this.filterVehicles();
  }

  selectVehicle(vehicle: Vehicle): void {
    this.form().get('vehicleId')?.setValue(vehicle.id);
    this.form().get('vehicleId')?.markAsTouched();
    this.vehicleLabel.set(`${vehicle.licensePlate} · ${vehicle.brand} ${vehicle.model}`);
    this.vehicleResultsOpen.set(false);
    if (this.customerId() !== vehicle.customerId) {
      this.form().get('customerId')?.setValue(vehicle.customerId);
      this.customerLabel.set(`${vehicle.customerFirstName} ${vehicle.customerLastName}`);
    }
  }

  clearCustomer(): void {
    this.form().get('customerId')?.setValue(null);
    this.customerLabel.set('');
    this.customerSearch.setValue('');
    this.clearVehicle();
    this.filterVehicles();
  }

  clearVehicle(): void {
    this.form().get('vehicleId')?.setValue(null);
    this.vehicleLabel.set('');
    this.vehicleSearch.setValue('');
  }

  /** Cierra el desplegable cuando el foco sale del autocomplete (click afuera o Tab). */
  closeOnFocusOut(event: FocusEvent, open: WritableSignal<boolean>): void {
    const container = event.currentTarget as HTMLElement;
    if (!container.contains(event.relatedTarget as Node | null)) open.set(false);
  }

  openVehicleCreation(): void {
    if (!this.customerId()) {
      this.errorMessage.set('Primero seleccioná o creá el cliente del vehículo');
      return;
    }
    this.creationMode.set('vehicle');
  }

  customerCreated(customer: Customer): void {
    this.creationMode.set(null);
    this.customers.update((items) => [customer, ...items].slice(0, 5));
    this.selectCustomer(customer);
  }

  vehicleCreated(vehicle: Vehicle): void {
    this.creationMode.set(null);
    this.allVehicleResults.update((items) => [vehicle, ...items]);
    this.selectVehicle(vehicle);
  }

  private configureSearches(): void {
    this.customerSearch.valueChanges
      .pipe(
        startWith(''),
        debounceTime(300),
        distinctUntilChanged(),
        tap(() => this.loadingCustomers.set(true)),
        switchMap((search) =>
          this.customersService.findAll(search).pipe(
            catchError((error: HttpErrorResponse) => {
              this.errorMessage.set(error.error?.message ?? 'No se pudieron buscar los clientes');
              return of([]);
            }),
          ),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((customers) => {
        this.customers.set(customers.slice(0, 5));
        this.loadingCustomers.set(false);
      });

    this.vehicleSearch.valueChanges
      .pipe(
        startWith(''),
        debounceTime(300),
        distinctUntilChanged(),
        tap(() => this.loadingVehicles.set(true)),
        switchMap((search) =>
          this.vehiclesService.findAll(search).pipe(
            catchError((error: HttpErrorResponse) => {
              this.errorMessage.set(error.error?.message ?? 'No se pudieron buscar los vehículos');
              return of([]);
            }),
          ),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((vehicles) => {
        this.allVehicleResults.set(vehicles);
        this.filterVehicles();
        this.loadingVehicles.set(false);
      });
  }

  private filterVehicles(): void {
    const customerId = this.customerId();
    this.vehicles.set(
      this.allVehicleResults()
        .filter((vehicle) => !customerId || vehicle.customerId === customerId)
        .slice(0, 5),
    );
  }

  private formatCustomer(customer: Customer): string {
    const details = [customer.documentNumber, customer.phone].filter(Boolean).join(' · ');
    return `${customer.firstName} ${customer.lastName}${details ? ` · ${details}` : ''}`;
  }
}
