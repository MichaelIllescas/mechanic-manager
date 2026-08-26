import { DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { NotificationService } from '../../../core/notifications/notification-service';
import { SupplyService } from '../supply-service';
import { Supply } from '../supply.models';

@Component({
  selector: 'app-supply-list',
  imports: [RouterLink, DecimalPipe],
  templateUrl: './supply-list.html',
  styleUrl: './supply-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SupplyList implements OnInit {
  private readonly supplyService = inject(SupplyService);
  private readonly notifications = inject(NotificationService);

  readonly supplies = signal<Supply[]>([]);
  readonly updatingId = signal<number | null>(null);
  readonly onlyActive = signal(false);

  ngOnInit(): void {
    this.load();
  }

  toggleFilter(): void {
    this.onlyActive.update((value) => !value);
    this.load();
  }

  toggleStatus(supply: Supply): void {
    const active = !supply.active;

    this.updatingId.set(supply.id);

    this.supplyService.updateStatus(supply.id, { active }).subscribe({
      next: (updated) => {
        this.updatingId.set(null);
        this.applyUpdate(updated);
        this.notifications.success(
          updated.active
            ? `${updated.name} fue activado`
            : `${updated.name} fue desactivado`,
        );
      },
      error: (err: HttpErrorResponse) => {
        this.updatingId.set(null);
        this.notifications.error(err.error?.message ?? 'No se pudo cambiar el estado');
      },
    });
  }

  private load(): void {
    this.supplyService.findAll(this.onlyActive() ? true : undefined).subscribe({
      next: (supplies) => this.supplies.set(supplies),
      error: (err: HttpErrorResponse) =>
        this.notifications.error(err.error?.message ?? 'No se pudieron cargar los insumos'),
    });
  }

  private applyUpdate(updated: Supply): void {
    if (this.onlyActive() && !updated.active) {
      this.supplies.update((list) => list.filter((item) => item.id !== updated.id));
      return;
    }

    this.supplies.update((list) =>
      list.map((item) => (item.id === updated.id ? updated : item)),
    );
  }
}
