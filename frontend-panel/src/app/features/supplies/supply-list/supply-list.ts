import { DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { startWith } from 'rxjs';

import { NotificationService } from '../../../core/notifications/notification-service';
import { SupplyService } from '../supply-service';
import { Supply } from '../supply.models';

@Component({
  selector: 'app-supply-list',
  imports: [RouterLink, DecimalPipe, ReactiveFormsModule],
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
  readonly searchControl = new FormControl('', { nonNullable: true });
  private readonly searchQuery = toSignal(
    this.searchControl.valueChanges.pipe(startWith(this.searchControl.value)),
    { initialValue: '' },
  );
  readonly filteredSupplies = computed(() => {
    const query = this.normalizeSearch(this.searchQuery());

    if (!query) {
      return this.supplies();
    }

    return this.supplies().filter((supply) => this.normalizeSearch(supply.name).includes(query));
  });

  ngOnInit(): void {
    this.load();
  }

  toggleFilter(): void {
    this.onlyActive.update((value) => !value);
    this.load();
  }

  clearSearch(): void {
    this.searchControl.setValue('');
  }

  toggleStatus(supply: Supply): void {
    const active = !supply.active;

    this.updatingId.set(supply.id);

    this.supplyService.updateStatus(supply.id, { active }).subscribe({
      next: (updated) => {
        this.updatingId.set(null);
        this.applyUpdate(updated);
        this.notifications.success(
          updated.active ? `${updated.name} fue activado` : `${updated.name} fue desactivado`,
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

    this.supplies.update((list) => list.map((item) => (item.id === updated.id ? updated : item)));
  }

  private normalizeSearch(value: string): string {
    return value.trim().toLocaleLowerCase('es');
  }
}
