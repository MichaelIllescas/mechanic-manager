import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { NotificationService } from '../../../core/notifications/notification-service';
import { VehicleService } from '../../vehicles/vehicle-service';
import { Vehicle } from '../../vehicles/vehicle.models';
import { AppointmentService } from '../appointment-service';
import { parseLocalDateTime, toIsoDate } from '../calendar.utils';

@Component({
  selector: 'app-appointment-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './appointment-form.html',
  styleUrl: './appointment-form.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppointmentForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly appointmentService = inject(AppointmentService);
  private readonly vehicleService = inject(VehicleService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly notifications = inject(NotificationService);

  private appointmentId: number | null = null;

  readonly isEdit = signal(false);
  readonly loadingVehicles = signal(false);
  readonly loadingAppointment = signal(false);
  readonly loading = computed(() => this.loadingVehicles() || this.loadingAppointment());
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly vehicles = signal<Vehicle[]>([]);
  readonly vehicleSearch = signal('');
  readonly vehicleResultsOpen = signal(false);
  readonly selectedVehicleLabel = signal<string | null>(null);

  readonly filteredVehicles = computed(() => {
    const search = this.vehicleSearch().trim().toLowerCase();

    if (!search) {
      return [];
    }

    return this.vehicles()
      .filter((vehicle) => this.vehicleMatchesSearch(vehicle, search))
      .slice(0, 8);
  });

  readonly form = this.fb.group({
    vehicleId: [null as number | null, [Validators.required, Validators.min(1)]],
    date: ['', [Validators.required]],
    time: ['', [Validators.required]],
    notes: ['', [Validators.maxLength(1000)]],
  });

  ngOnInit(): void {
    this.loadVehicles();

    const routeId = this.route.snapshot.paramMap.get('id');

    if (routeId === null) {
      this.applyPrefillFromQuery();
      return;
    }

    const appointmentId = Number(routeId);

    if (!Number.isInteger(appointmentId) || appointmentId <= 0) {
      this.errorMessage.set('El identificador del turno no es válido');
      return;
    }

    this.appointmentId = appointmentId;
    this.isEdit.set(true);
    this.loadAppointment(appointmentId);
  }

  onSubmit(): void {
    if (this.form.invalid || this.loading()) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);

    const { vehicleId, date, time, notes } = this.form.getRawValue();
    const scheduledAt = `${date}T${time}:00`;
    const trimmedNotes = notes?.trim() ? notes.trim() : null;

    const request = this.isEdit()
      ? this.appointmentService.update(this.appointmentId!, {
          scheduledAt,
          notes: trimmedNotes,
        })
      : this.appointmentService.create({
          vehicleId: vehicleId!,
          scheduledAt,
          notes: trimmedNotes,
        });

    request.subscribe({
      next: () => {
        this.notifications.success(
          this.isEdit() ? 'Turno reprogramado correctamente' : 'Turno agendado correctamente',
        );
        void this.router.navigate(['/appointments']);
      },
      error: (error: HttpErrorResponse) => {
        this.saving.set(false);
        this.errorMessage.set(
          error.error?.message ??
            (this.isEdit() ? 'No se pudo reprogramar el turno' : 'No se pudo agendar el turno'),
        );
      },
    });
  }

  onVehicleSearch(value: string): void {
    this.vehicleSearch.set(value);
    this.vehicleResultsOpen.set(true);
  }

  openVehicleResults(): void {
    if (!this.selectedVehicleLabel() && this.vehicleSearch().trim()) {
      this.vehicleResultsOpen.set(true);
    }
  }

  selectVehicle(vehicle: Vehicle): void {
    this.form.controls.vehicleId.setValue(vehicle.id);
    this.form.controls.vehicleId.markAsTouched();
    this.selectedVehicleLabel.set(this.formatVehicle(vehicle));
    this.vehicleSearch.set('');
    this.vehicleResultsOpen.set(false);
  }

  changeVehicle(): void {
    this.form.controls.vehicleId.setValue(null);
    this.form.controls.vehicleId.markAsTouched();
    this.selectedVehicleLabel.set(null);
    this.vehicleSearch.set('');
    this.vehicleResultsOpen.set(false);
  }

  private loadVehicles(): void {
    this.loadingVehicles.set(true);

    this.vehicleService.findAll().subscribe({
      next: (vehicles) => {
        this.vehicles.set(vehicles);
        this.loadingVehicles.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.loadingVehicles.set(false);
        this.errorMessage.set(error.error?.message ?? 'No se pudieron cargar los vehículos');
      },
    });
  }

  private loadAppointment(appointmentId: number): void {
    this.loadingAppointment.set(true);

    this.appointmentService.findById(appointmentId).subscribe({
      next: (appointment) => {
        this.loadingAppointment.set(false);

        const scheduledAt = parseLocalDateTime(appointment.scheduledAt);

        this.form.patchValue({
          vehicleId: appointment.vehicleId,
          date: toIsoDate(scheduledAt),
          time: appointment.scheduledAt.slice(11, 16),
          notes: appointment.notes ?? '',
        });

        this.selectedVehicleLabel.set(
          `${appointment.licensePlate} — ${appointment.brand} ${appointment.model}`,
        );
      },
      error: (error: HttpErrorResponse) => {
        this.loadingAppointment.set(false);
        this.errorMessage.set(error.error?.message ?? 'No se pudo cargar el turno');
      },
    });
  }

  /**
   * El calendario puede abrir el alta con el día ya elegido
   * (/appointments/nuevo?date=2026-09-10).
   */
  private applyPrefillFromQuery(): void {
    const date = this.route.snapshot.queryParamMap.get('date');

    this.form.patchValue({
      date: date ?? toIsoDate(new Date()),
      time: '09:00',
    });
  }

  private vehicleMatchesSearch(vehicle: Vehicle, search: string): boolean {
    return [
      vehicle.licensePlate,
      vehicle.brand,
      vehicle.model,
      `${vehicle.customerFirstName} ${vehicle.customerLastName}`,
    ].some((field) => field.toLowerCase().includes(search));
  }

  private formatVehicle(vehicle: Vehicle): string {
    return `${vehicle.licensePlate} — ${vehicle.brand} ${vehicle.model}`;
  }
}
