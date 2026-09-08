import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { NotificationService } from '../../../../../core/notifications/notification-service';
import { VehicleService } from '../../../../vehicles/vehicle-service';
import { CreateVehicleRequest, Vehicle, VehicleType } from '../../../../vehicles/vehicle.models';

@Component({
  selector: 'app-vehicle-quick-create',
  imports: [ReactiveFormsModule],
  templateUrl: './vehicle-quick-create.html',
  styleUrl: '../quick-create.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VehicleQuickCreate {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(VehicleService);
  private readonly notifications = inject(NotificationService);

  readonly customerId = input.required<number>();
  readonly saved = output<Vehicle>();
  readonly cancelled = output<void>();
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);
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
    licensePlate: ['', [Validators.required, Validators.maxLength(20)]],
    brand: ['', [Validators.required, Validators.maxLength(80)]],
    model: ['', [Validators.required, Validators.maxLength(120)]],
    year: [null as number | null, [Validators.min(1886), Validators.max(9999)]],
    vehicleType: ['CAR' as VehicleType, Validators.required],
    mileage: [null as number | null, Validators.min(0)],
    chassisNumber: ['', Validators.maxLength(100)],
    engineNumber: ['', Validators.maxLength(100)],
    notes: ['', Validators.maxLength(1000)],
  });

  save(): void {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.errorMessage.set(null);
    this.service.create(this.payload()).subscribe({
      next: (vehicle) => {
        this.saving.set(false);
        this.notifications.success('Vehículo registrado correctamente');
        this.saved.emit(vehicle);
      },
      error: (error: HttpErrorResponse) => {
        this.saving.set(false);
        this.errorMessage.set(error.error?.message ?? 'No se pudo registrar el vehículo');
      },
    });
  }

  cancel(): void {
    if (!this.saving()) this.cancelled.emit();
  }

  private payload(): CreateVehicleRequest {
    const value = this.form.getRawValue();
    return {
      customerId: this.customerId(),
      licensePlate: value.licensePlate!.trim().toUpperCase(),
      brand: value.brand!.trim(),
      model: value.model!.trim(),
      year: value.year,
      chassisNumber: this.optional(value.chassisNumber),
      engineNumber: this.optional(value.engineNumber),
      mileage: value.mileage,
      vehicleType: value.vehicleType!,
      notes: this.optional(value.notes),
    };
  }

  private optional(value: string | null | undefined): string | null {
    return value?.trim() || null;
  }
}
