import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { startWith } from 'rxjs';

import { NotificationService } from '../../../core/notifications/notification-service';
import { ServiceTypeService } from '../service-type-service';
import { ServiceType } from '../service-type.models';

@Component({
  selector: 'app-service-type-list',
  imports: [RouterLink, DecimalPipe, ReactiveFormsModule],
  templateUrl: './service-type-list.html',
  styleUrl: './service-type-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ServiceTypeList implements OnInit {
  private readonly serviceTypeService = inject(ServiceTypeService);
  private readonly notifications = inject(NotificationService);

  readonly serviceTypes = signal<ServiceType[]>([]);
  readonly updatingId = signal<number | null>(null);
  readonly onlyActive = signal(false);
  readonly searchControl = new FormControl('', { nonNullable: true });
  private readonly searchQuery = toSignal(
    this.searchControl.valueChanges.pipe(startWith(this.searchControl.value)),
    { initialValue: '' },
  );
  readonly filteredServiceTypes = computed(() => {
    const query = this.normalizeSearch(this.searchQuery());

    if (!query) {
      return this.serviceTypes();
    }

    return this.serviceTypes().filter((serviceType) => {
      const searchableText = this.normalizeSearch(
        `${serviceType.name} ${serviceType.description ?? ''}`,
      );

      return searchableText.includes(query);
    });
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

  toggleStatus(serviceType: ServiceType): void {
    const active = !serviceType.active;

    this.updatingId.set(serviceType.id);

    this.serviceTypeService.updateStatus(serviceType.id, { active }).subscribe({
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
    this.serviceTypeService.findAll(this.onlyActive() ? true : undefined).subscribe({
      next: (serviceTypes) => this.serviceTypes.set(serviceTypes),
    });
  }

  /**
   * Con el filtro de activos puesto, desactivar un servicio lo saca del listado.
   * Sin filtro solo cambia el estado de la fila.
   */
  private applyUpdate(updated: ServiceType): void {
    if (this.onlyActive() && !updated.active) {
      this.serviceTypes.update((list) => list.filter((item) => item.id !== updated.id));
      return;
    }

    this.serviceTypes.update((list) =>
      list.map((item) => (item.id === updated.id ? updated : item)),
    );
  }

  private normalizeSearch(value: string): string {
    return value.trim().toLocaleLowerCase('es');
  }
}
