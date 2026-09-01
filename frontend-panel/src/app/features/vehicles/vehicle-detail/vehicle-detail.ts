import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { VehicleService } from '../vehicle-service';
import { Vehicle, VehicleType } from '../vehicle.models';

@Component({
  selector: 'app-vehicle-detail',
  imports: [RouterLink],
  templateUrl: './vehicle-detail.html',
  styleUrl: './vehicle-detail.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VehicleDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly vehicleService = inject(VehicleService);

  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly vehicle = signal<Vehicle | null>(null);

  ngOnInit(): void {
    const routeId = this.route.snapshot.paramMap.get('id');
    const vehicleId = Number(routeId);

    if (!Number.isInteger(vehicleId) || vehicleId <= 0) {
      this.errorMessage.set('El identificador del vehículo no es válido');
      return;
    }

    this.loadVehicle(vehicleId);
  }

  vehicleTypeLabel(type: VehicleType): string {
    const labels: Record<VehicleType, string> = {
      CAR: 'Auto',
      SUV: 'SUV',
      PICKUP: 'Pickup',
      VAN: 'Utilitario',
      TRUCK: 'Camión',
      MOTORCYCLE: 'Motocicleta',
      OTHER: 'Otro',
    };

    return labels[type];
  }

  displayValue(value: number | string | null): string {
    return value === null || value === '' ? 'Sin informar' : String(value);
  }

  displayDateTime(value: string | null): string {
    if (!value) {
      return 'Sin informar';
    }

    return new Intl.DateTimeFormat('es-AR', {
      dateStyle: 'short',
      timeStyle: 'short',
    }).format(new Date(value));
  }

  private loadVehicle(vehicleId: number): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.vehicleService.findFromListById(vehicleId).subscribe({
      next: (vehicle) => {
        this.loading.set(false);

        if (!vehicle) {
          this.errorMessage.set('No se encontró el vehículo solicitado');
          return;
        }

        this.vehicle.set(vehicle);
      },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);
        this.errorMessage.set(error.error?.message ?? 'No se pudo cargar el vehículo');
      },
    });
  }
}
