import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { NotificationService } from '../../../core/notifications/notification-service';
import { Workshop } from '../../workshop/workshop.models';
import { WorkshopService } from '../../workshop/workshop-service';
import { WorkOrderService } from '../work-order-service';
import { WorkOrder, WorkOrderStatus } from '../work-order.models';

@Component({
  selector: 'app-work-order-detail',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './work-order-detail.html',
  styleUrl: './work-order-detail.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkOrderDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly workOrderService = inject(WorkOrderService);
  private readonly workshopService = inject(WorkshopService);
  private readonly notifications = inject(NotificationService);
  private readonly changeDetector = inject(ChangeDetectorRef);

  readonly workOrder = signal<WorkOrder | null>(null);
  readonly workshop = signal<Workshop | null>(null);
  readonly loading = signal(false);
  readonly updatingStatus = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly downloadedAt = signal<Date | null>(null);

  ngOnInit(): void {
    const workOrderId = Number(this.route.snapshot.paramMap.get('id'));

    if (!Number.isInteger(workOrderId) || workOrderId <= 0) {
      this.errorMessage.set('El identificador del trabajo realizado no es válido');
      return;
    }

    this.load(workOrderId);
    this.workshopService.findMine().subscribe({
      next: (workshop) => this.workshop.set(workshop),
      error: () => this.workshop.set(null),
    });
  }

  downloadWorkOrder(): void {
    const previousTitle = document.title;

    this.downloadedAt.set(new Date());
    this.changeDetector.detectChanges();
    document.title = '';
    window.print();
    document.title = previousTitle;
  }

  updateStatus(status: WorkOrderStatus): void {
    const workOrder = this.workOrder();
    if (!workOrder || workOrder.status === status || this.updatingStatus()) {
      return;
    }

    this.updatingStatus.set(true);
    this.errorMessage.set(null);

    this.workOrderService.updateStatus(workOrder.id, { status }).subscribe({
      next: (updated) => {
        this.workOrder.set(updated);
        this.updatingStatus.set(false);
        this.notifications.success(
          `Trabajo marcado como ${this.statusLabel(status).toLowerCase()}`,
        );
      },
      error: (error: HttpErrorResponse) => {
        this.updatingStatus.set(false);
        this.errorMessage.set(error.error?.message ?? 'No se pudo actualizar el estado');
      },
    });
  }

  formatNumber(id: number): string {
    return id.toString().padStart(4, '0');
  }

  statusLabel(status: WorkOrderStatus): string {
    const labels: Record<WorkOrderStatus, string> = {
      COMPLETED: 'Completado',
      CANCELLED: 'Cancelado',
    };

    return labels[status];
  }

  private load(workOrderId: number): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.workOrderService.findById(workOrderId).subscribe({
      next: (workOrder) => {
        this.workOrder.set(workOrder);
        this.loading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);
        this.errorMessage.set(error.error?.message ?? 'No se pudo cargar el trabajo realizado');
      },
    });
  }
}
