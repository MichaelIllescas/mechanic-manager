import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Observable, catchError, debounceTime, distinctUntilChanged, of, switchMap, tap } from 'rxjs';

import { CustomerService } from '../customers/customer-service';
import { Customer } from '../customers/customer.models';
import { VehicleService } from '../vehicles/vehicle-service';
import { Vehicle } from '../vehicles/vehicle.models';
import { WorkOrder, WorkOrderStatus } from '../work-orders/work-order.models';
import { HistoryService } from './history-service';

export type HistoryTab = 'customer' | 'vehicle';

const MAX_RESULTS = 8;

@Component({
  selector: 'app-history',
  imports: [CurrencyPipe, DatePipe, ReactiveFormsModule, RouterLink],
  templateUrl: './history.html',
  styleUrl: './history.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class History implements OnInit {
  private readonly customerService = inject(CustomerService);
  private readonly vehicleService = inject(VehicleService);
  private readonly historyService = inject(HistoryService);
  private readonly destroyRef = inject(DestroyRef);

  readonly tab = signal<HistoryTab>('customer');
  readonly searchControl = new FormControl('', { nonNullable: true });
  readonly customerResults = signal<Customer[]>([]);
  readonly vehicleResults = signal<Vehicle[]>([]);
  readonly searching = signal(false);
  readonly selectedCustomer = signal<Customer | null>(null);
  readonly selectedVehicle = signal<Vehicle | null>(null);
  readonly workOrders = signal<WorkOrder[]>([]);
  readonly loadingHistory = signal(false);
  readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.searchControl.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        tap(() => this.searching.set(true)),
        switchMap((term) => this.search(term)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => this.searching.set(false));
  }

  selectTab(tab: HistoryTab): void {
    if (this.tab() === tab) {
      return;
    }

    this.tab.set(tab);
    this.reset();
  }

  selectCustomer(customer: Customer): void {
    this.selectedVehicle.set(null);
    this.selectedCustomer.set(customer);
    this.loadHistory(this.historyService.findByCustomer(customer.id));
  }

  selectVehicle(vehicle: Vehicle): void {
    this.selectedCustomer.set(null);
    this.selectedVehicle.set(vehicle);
    this.loadHistory(this.historyService.findByVehicle(vehicle.id));
  }

  clearSelection(): void {
    this.selectedCustomer.set(null);
    this.selectedVehicle.set(null);
    this.workOrders.set([]);
    this.errorMessage.set(null);
  }

  totalSpent(): number {
    return this.workOrders()
      .filter((workOrder) => workOrder.status === 'COMPLETED')
      .reduce((sum, workOrder) => sum + workOrder.total, 0);
  }

  statusLabel(status: WorkOrderStatus): string {
    return status === 'COMPLETED' ? 'Completado' : 'Cancelado';
  }

  private search(term: string): Observable<unknown> {
    const normalized = term.trim();

    if (!normalized) {
      this.customerResults.set([]);
      this.vehicleResults.set([]);
      return of(null);
    }

    this.errorMessage.set(null);

    if (this.tab() === 'customer') {
      return this.customerService.findAll(normalized).pipe(
        tap((customers) => this.customerResults.set(customers.slice(0, MAX_RESULTS))),
        catchError(() => this.failSearch()),
      );
    }

    return this.vehicleService.findAll(normalized).pipe(
      tap((vehicles) => this.vehicleResults.set(vehicles.slice(0, MAX_RESULTS))),
      catchError(() => this.failSearch()),
    );
  }

  private failSearch(): Observable<null> {
    this.errorMessage.set('No se pudo realizar la búsqueda');
    return of(null);
  }

  private loadHistory(request: Observable<WorkOrder[]>): void {
    this.loadingHistory.set(true);
    this.errorMessage.set(null);
    this.workOrders.set([]);

    request.subscribe({
      next: (workOrders) => {
        this.workOrders.set(workOrders);
        this.loadingHistory.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage.set(error.error?.message ?? 'No se pudo cargar el historial');
        this.loadingHistory.set(false);
      },
    });
  }

  private reset(): void {
    this.searchControl.setValue('', { emitEvent: false });
    this.customerResults.set([]);
    this.vehicleResults.set([]);
    this.clearSelection();
  }
}
