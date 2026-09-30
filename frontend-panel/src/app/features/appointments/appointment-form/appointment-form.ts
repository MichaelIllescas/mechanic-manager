import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, map, of, switchMap } from 'rxjs';

import { NotificationService } from '../../../core/notifications/notification-service';
import { QuoteService } from '../../quotes/quote-service';
import { Quote, QuoteSummary } from '../../quotes/quote.models';
import { VehicleService } from '../../vehicles/vehicle-service';
import { Vehicle } from '../../vehicles/vehicle.models';
import { AppointmentService } from '../appointment-service';
import { parseLocalDateTime, toIsoDate } from '../calendar.utils';
import { QuoteQuickCreate } from './quote-quick-create/quote-quick-create';
import { VehicleQuickCreate } from './vehicle-quick-create/vehicle-quick-create';

@Component({
  selector: 'app-appointment-form',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    CurrencyPipe,
    DatePipe,
    QuoteQuickCreate,
    VehicleQuickCreate,
  ],
  templateUrl: './appointment-form.html',
  styleUrl: './appointment-form.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppointmentForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly appointmentService = inject(AppointmentService);
  private readonly vehicleService = inject(VehicleService);
  private readonly quoteService = inject(QuoteService);
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

  readonly selectedVehicleId = signal<number | null>(null);
  readonly selectedCustomerId = signal<number | null>(null);
  /**
   * Presupuestos del vehículo elegido que se pueden vincular: aprobados, sin
   * trabajo realizado y sin otro turno activo. El filtro lo resuelve el backend.
   */
  readonly availableQuotes = signal<QuoteSummary[]>([]);
  readonly showQuoteQuickCreate = signal(false);
  readonly showVehicleQuickCreate = signal(false);

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
    quoteId: [null as number | null],
    date: ['', [Validators.required]],
    time: ['', [Validators.required]],
    notes: ['', [Validators.maxLength(1000)]],
  });

  constructor() {
    toObservable(this.selectedVehicleId)
      .pipe(
        switchMap((vehicleId) =>
          vehicleId === null
            ? of([])
            : this.quoteService.findAvailableForAppointment(vehicleId).pipe(
                map((page) => page.content),
                catchError(() => of([])),
              ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((quotes) => this.availableQuotes.set(quotes));
  }

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

    const { vehicleId, quoteId, date, time, notes } = this.form.getRawValue();
    const scheduledAt = `${date}T${time}:00`;
    const trimmedNotes = notes?.trim() ? notes.trim() : null;

    const request = this.isEdit()
      ? this.appointmentService.update(this.appointmentId!, {
          scheduledAt,
          notes: trimmedNotes,
        })
      : this.appointmentService.create({
          vehicleId: vehicleId!,
          quoteId,
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
    this.form.controls.quoteId.setValue(null);
    this.selectedVehicleLabel.set(this.formatVehicle(vehicle));
    this.selectedVehicleId.set(vehicle.id);
    this.selectedCustomerId.set(vehicle.customerId);
    this.vehicleSearch.set('');
    this.vehicleResultsOpen.set(false);
  }

  changeVehicle(): void {
    this.form.controls.vehicleId.setValue(null);
    this.form.controls.vehicleId.markAsTouched();
    this.form.controls.quoteId.setValue(null);
    this.selectedVehicleLabel.set(null);
    this.selectedVehicleId.set(null);
    this.selectedCustomerId.set(null);
    this.vehicleSearch.set('');
    this.vehicleResultsOpen.set(false);
  }

  openVehicleQuickCreate(): void {
    this.vehicleResultsOpen.set(false);
    this.showVehicleQuickCreate.set(true);
  }

  closeVehicleQuickCreate(): void {
    this.showVehicleQuickCreate.set(false);
  }

  vehicleQuickCreated(vehicle: Vehicle): void {
    this.vehicles.update((vehicles) => [...vehicles, vehicle]);
    this.selectVehicle(vehicle);
    this.showVehicleQuickCreate.set(false);
  }

  openQuoteQuickCreate(): void {
    this.showQuoteQuickCreate.set(true);
  }

  closeQuoteQuickCreate(): void {
    this.showQuoteQuickCreate.set(false);
  }

  quoteQuickCreated(quote: Quote): void {
    this.showQuoteQuickCreate.set(false);
    this.availableQuotes.update((quotes) => [
      {
        id: quote.id,
        number: quote.number,
        customerId: quote.customerId,
        customerName: quote.customerName,
        vehicleId: quote.vehicleId,
        vehicleLicensePlate: quote.vehicleLicensePlate,
        validUntil: quote.validUntil,
        status: quote.status,
        total: quote.total,
        createdAt: quote.createdAt,
      },
      ...quotes,
    ]);
    this.form.controls.quoteId.setValue(quote.id);
  }

  private loadVehicles(): void {
    this.loadingVehicles.set(true);

    this.vehicleService.findAll().subscribe({
      next: (vehicles) => {
        this.vehicles.set(vehicles);
        this.loadingVehicles.set(false);
        this.prefillVehicleIfMissing(vehicles);
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
    const vehicleId = Number(this.route.snapshot.queryParamMap.get('vehicleId'));
    const quoteId = Number(this.route.snapshot.queryParamMap.get('quoteId'));

    this.form.patchValue({
      date: date ?? toIsoDate(new Date()),
      time: '09:00',
    });

    if (Number.isInteger(vehicleId) && vehicleId > 0) {
      this.form.controls.vehicleId.setValue(vehicleId);
      this.selectedVehicleId.set(vehicleId);
    }

    if (Number.isInteger(quoteId) && quoteId > 0) {
      this.form.controls.quoteId.setValue(quoteId);
    }

    this.prefillVehicleIfMissing(this.vehicles());
  }

  private prefillVehicleIfMissing(vehicles: Vehicle[]): void {
    const vehicleId = this.form.controls.vehicleId.value;
    if (!vehicleId || this.selectedVehicleLabel()) {
      return;
    }

    const vehicle = vehicles.find((candidate) => candidate.id === vehicleId);
    if (!vehicle) {
      return;
    }

    this.selectedVehicleLabel.set(this.formatVehicle(vehicle));
    this.selectedVehicleId.set(vehicle.id);
    this.selectedCustomerId.set(vehicle.customerId);
  }

  formatNumber(number: number): string {
    return number.toString().padStart(4, '0');
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
