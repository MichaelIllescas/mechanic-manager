import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  HostListener,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import {
  Subject,
  catchError,
  debounceTime,
  distinctUntilChanged,
  of,
  startWith,
  switchMap,
  tap,
} from 'rxjs';

import { ConfirmService } from '../../../core/confirm/confirm-service';
import { NotificationService } from '../../../core/notifications/notification-service';
import { WorkOrderService } from '../work-order-service';
import { WorkOrderPage, WorkOrderStatus, WorkOrderSummary } from '../work-order.models';

interface WorkOrderSearchRequest {
  search: string;
  status?: WorkOrderStatus;
  vehicleId?: number;
  page: number;
}

@Component({
  selector: 'app-work-order-list',
  imports: [CurrencyPipe, DatePipe, ReactiveFormsModule, RouterLink],
  templateUrl: './work-order-list.html',
  styleUrl: './work-order-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkOrderList implements OnInit {
  private readonly workOrderService = inject(WorkOrderService);
  private readonly notifications = inject(NotificationService);
  private readonly confirmService = inject(ConfirmService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly route = inject(ActivatedRoute);
  private readonly requests = new Subject<WorkOrderSearchRequest>();

  readonly workOrders = signal<WorkOrderSummary[]>([]);
  readonly selectedStatus = signal<WorkOrderStatus | null>(null);
  readonly vehicleFilter = signal<{ id: number } | null>(null);
  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly totalElements = signal(0);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly searchControl = new FormControl('', { nonNullable: true });
  readonly openMenuId = signal<number | null>(null);
  readonly menuPosition = signal<{ top?: number; bottom?: number; right: number } | null>(null);
  readonly updatingStatusId = signal<number | null>(null);
  readonly deletingId = signal<number | null>(null);

  ngOnInit(): void {
    const vehicleId = Number(this.route.snapshot.queryParamMap.get('vehicleId'));
    if (Number.isInteger(vehicleId) && vehicleId > 0) {
      this.vehicleFilter.set({ id: vehicleId });
    }

    this.requests
      .pipe(
        tap(() => {
          this.loading.set(true);
          this.errorMessage.set(null);
        }),
        switchMap(({ search, status, vehicleId, page }) =>
          this.workOrderService.search(search, status, vehicleId, undefined, undefined, page).pipe(
            catchError((error: HttpErrorResponse) => {
              this.errorMessage.set(
                error.error?.message ?? 'No se pudieron cargar los trabajos realizados',
              );
              return of(this.emptyPage(page));
            }),
          ),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((result) => this.applyPage(result));

    this.searchControl.valueChanges
      .pipe(
        startWith(this.searchControl.value),
        debounceTime(300),
        distinctUntilChanged(),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => this.loadPage(0));
  }

  filterByStatus(status: WorkOrderStatus | null): void {
    if (this.selectedStatus() === status) {
      return;
    }

    this.selectedStatus.set(status);
    this.loadPage(0);
  }

  clearSearch(): void {
    this.searchControl.setValue('');
  }

  clearVehicleFilter(): void {
    this.vehicleFilter.set(null);
    this.loadPage(0);
  }

  previousPage(): void {
    if (this.page() > 0) {
      this.loadPage(this.page() - 1);
    }
  }

  nextPage(): void {
    if (this.page() + 1 < this.totalPages()) {
      this.loadPage(this.page() + 1);
    }
  }

  toggleMenu(workOrderId: number, event: MouseEvent): void {
    if (this.openMenuId() === workOrderId) {
      this.closeMenu();
      return;
    }

    const button = event.currentTarget as HTMLElement;
    const rect = button.getBoundingClientRect();
    const dropdownHeight = 160;
    const right = window.innerWidth - rect.right;

    if (rect.bottom + 8 + dropdownHeight > window.innerHeight) {
      this.menuPosition.set({ bottom: window.innerHeight - rect.top + 8, right });
    } else {
      this.menuPosition.set({ top: rect.bottom + 8, right });
    }

    this.openMenuId.set(workOrderId);
  }

  closeMenu(): void {
    this.openMenuId.set(null);
    this.menuPosition.set(null);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (this.openMenuId() === null) {
      return;
    }

    const target = event.target as HTMLElement;
    if (!target.closest('.status-menu')) {
      this.closeMenu();
    }
  }

  changeStatus(workOrder: WorkOrderSummary, status: WorkOrderStatus): void {
    this.closeMenu();

    if (workOrder.status === status || this.updatingStatusId()) {
      return;
    }

    this.updatingStatusId.set(workOrder.id);

    this.workOrderService.updateStatus(workOrder.id, { status }).subscribe({
      next: (updated) => {
        this.workOrders.update((list) =>
          list.map((item) => (item.id === updated.id ? { ...item, status: updated.status } : item)),
        );
        this.updatingStatusId.set(null);
        this.notifications.success(
          `Trabajo marcado como ${this.statusLabel(status).toLowerCase()}`,
        );
      },
      error: (error: HttpErrorResponse) => {
        this.updatingStatusId.set(null);
        this.notifications.error(error.error?.message ?? 'No se pudo actualizar el estado');
      },
    });
  }

  async deleteWorkOrder(workOrder: WorkOrderSummary): Promise<void> {
    this.closeMenu();

    if (this.deletingId()) {
      return;
    }

    const confirmed = await this.confirmService.confirm({
      title: 'Eliminar trabajo realizado',
      message: `¿Eliminar el trabajo realizado de ${workOrder.customerName} (${workOrder.vehicleLicensePlate})? Esta acción no se puede deshacer.`,
      confirmLabel: 'Eliminar',
      danger: true,
    });
    if (!confirmed) {
      return;
    }

    this.deletingId.set(workOrder.id);

    this.workOrderService.delete(workOrder.id).subscribe({
      next: () => {
        this.deletingId.set(null);
        this.workOrders.update((list) => list.filter((item) => item.id !== workOrder.id));
        this.totalElements.update((total) => Math.max(0, total - 1));
        this.notifications.success('Trabajo realizado eliminado correctamente');
      },
      error: (error: HttpErrorResponse) => {
        this.deletingId.set(null);
        this.notifications.error(
          error.error?.message ?? 'No se pudo eliminar el trabajo realizado',
        );
      },
    });
  }

  statusLabel(status: WorkOrderStatus): string {
    const labels: Record<WorkOrderStatus, string> = {
      COMPLETED: 'Completado',
      CANCELLED: 'Cancelado',
    };

    return labels[status];
  }

  private loadPage(page: number): void {
    this.requests.next({
      search: this.searchControl.value,
      status: this.selectedStatus() ?? undefined,
      vehicleId: this.vehicleFilter()?.id,
      page,
    });
  }

  private applyPage(result: WorkOrderPage): void {
    this.workOrders.set(result.content);
    this.page.set(result.page);
    this.totalPages.set(result.totalPages);
    this.totalElements.set(result.totalElements);
    this.loading.set(false);
  }

  private emptyPage(page: number): WorkOrderPage {
    return {
      content: [],
      page,
      size: 20,
      totalElements: 0,
      totalPages: 0,
    };
  }
}
