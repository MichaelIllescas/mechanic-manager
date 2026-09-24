import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { nonNegativeTotal } from '../../../shared/utils/items-total';
import { scrollToFirstError } from '../../../shared/utils/scroll-to-error';

import { NotificationService } from '../../../core/notifications/notification-service';
import { QuoteService } from '../../quotes/quote-service';
import { VehicleService } from '../../vehicles/vehicle-service';
import { Vehicle } from '../../vehicles/vehicle.models';
import { WorkOrderService } from '../work-order-service';
import { CreateWorkOrderRequest, WorkOrderItemRequest } from '../work-order.models';
import { WorkOrderItemsEditor } from './work-order-items-editor/work-order-items-editor';

@Component({
  selector: 'app-work-order-form',
  imports: [ReactiveFormsModule, RouterLink, WorkOrderItemsEditor],
  templateUrl: './work-order-form.html',
  styleUrl: './work-order-form.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkOrderForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly workOrderService = inject(WorkOrderService);
  private readonly quoteService = inject(QuoteService);
  private readonly vehicleService = inject(VehicleService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly notifications = inject(NotificationService);

  private workOrderId: number | null = null;
  private appointmentId: number | null = null;
  private quoteId: number | null = null;

  readonly isEdit = signal(false);
  readonly loadingVehicles = signal(false);
  readonly loadingWorkOrder = signal(false);
  readonly loadingQuote = signal(false);
  readonly loading = computed(
    () => this.loadingVehicles() || this.loadingWorkOrder() || this.loadingQuote(),
  );
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
    performedAt: ['', [Validators.required]],
    mileage: [null as number | null, [Validators.min(0)]],
    notes: ['', Validators.maxLength(2000)],
    items: this.fb.array([], [Validators.required, nonNegativeTotal]),
  });

  get items(): FormArray {
    return this.form.controls.items;
  }

  ngOnInit(): void {
    this.loadVehicles();

    const routeId = this.route.snapshot.paramMap.get('id');

    if (routeId === null) {
      this.applyPrefillFromQuery();
      return;
    }

    const id = Number(routeId);
    if (!Number.isInteger(id) || id <= 0) {
      this.errorMessage.set('El identificador del trabajo realizado no es válido');
      return;
    }
    this.workOrderId = id;
    this.isEdit.set(true);
    this.load(id);
  }

  onSubmit(): void {
    if (this.form.invalid || this.items.length === 0 || this.loading()) {
      this.form.markAllAsTouched();
      if (this.items.length === 0) this.errorMessage.set('Agregá al menos un servicio o insumo');
      else if (this.items.hasError('negativeTotal')) {
        this.errorMessage.set('El descuento no puede superar el total del trabajo realizado');
      }
      scrollToFirstError();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);

    const value = this.form.getRawValue();
    const items: WorkOrderItemRequest[] = this.items.controls.map((item) => ({
      serviceTypeId: item.get('serviceTypeId')?.value ?? null,
      supplyId: item.get('supplyId')?.value ?? null,
      description: String(item.get('description')?.value ?? '').trim(),
      quantity: Number(item.get('quantity')?.value),
      unitPrice: Number(item.get('unitPrice')?.value),
    }));

    const operation = this.isEdit()
      ? this.workOrderService.update(this.workOrderId!, {
          performedAt: value.performedAt!,
          mileage: value.mileage,
          notes: value.notes?.trim() || null,
          items,
        })
      : this.workOrderService.create(this.createPayload(value, items));

    operation.subscribe({
      next: (workOrder) => {
        this.notifications.success(
          this.isEdit()
            ? 'Trabajo actualizado correctamente'
            : 'Trabajo registrado correctamente',
        );
        void this.router.navigate(['/work-orders', workOrder.id]);
      },
      error: (error: HttpErrorResponse) => {
        this.saving.set(false);
        this.errorMessage.set(
          error.error?.message ??
            `No se pudo ${this.isEdit() ? 'actualizar' : 'registrar'} el trabajo`,
        );
        scrollToFirstError();
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

  private createPayload(
    value: ReturnType<typeof this.form.getRawValue>,
    items: WorkOrderItemRequest[],
  ): CreateWorkOrderRequest {
    return {
      vehicleId: value.vehicleId!,
      appointmentId: this.appointmentId,
      quoteId: this.quoteId,
      performedAt: value.performedAt!,
      mileage: value.mileage,
      notes: value.notes?.trim() || null,
      items,
    };
  }

  private loadVehicles(): void {
    this.loadingVehicles.set(true);

    this.vehicleService.findAll().subscribe({
      next: (vehicles) => {
        this.vehicles.set(vehicles);
        this.loadingVehicles.set(false);
        this.prefillVehicleLabelIfMissing(vehicles);
      },
      error: (error: HttpErrorResponse) => {
        this.loadingVehicles.set(false);
        this.errorMessage.set(error.error?.message ?? 'No se pudieron cargar los vehículos');
      },
    });
  }

  private load(id: number): void {
    this.loadingWorkOrder.set(true);
    this.workOrderService.findById(id).subscribe({
      next: (workOrder) => {
        this.form.patchValue({
          vehicleId: workOrder.vehicleId,
          performedAt: workOrder.performedAt,
          mileage: workOrder.mileage,
          notes: workOrder.notes ?? '',
        });
        this.selectedVehicleLabel.set(
          `${workOrder.vehicleLicensePlate} · ${workOrder.vehicleDescription}`,
        );
        this.items.clear();
        workOrder.items.forEach((item) => this.items.push(this.createItem(item)));
        this.loadingWorkOrder.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.loadingWorkOrder.set(false);
        this.errorMessage.set(error.error?.message ?? 'No se pudo cargar el trabajo realizado');
      },
    });
  }

  private createItem(item: WorkOrderItemRequest) {
    return this.fb.group({
      serviceTypeId: [item.serviceTypeId],
      supplyId: [item.supplyId],
      description: [item.description, [Validators.required, Validators.maxLength(255)]],
      quantity: [item.quantity, [Validators.required, Validators.min(0.01)]],
      unitPrice: [
        item.unitPrice,
        item.serviceTypeId || item.supplyId
          ? [Validators.required, Validators.min(0)]
          : [Validators.required],
      ],
    });
  }

  /**
   * Puede abrirse ya vinculado a un turno o presupuesto
   * (/work-orders/nuevo?appointmentId=1&vehicleId=2), sin repetir la búsqueda de vehículo.
   */
  private applyPrefillFromQuery(): void {
    const appointmentId = Number(this.route.snapshot.queryParamMap.get('appointmentId'));
    const quoteId = Number(this.route.snapshot.queryParamMap.get('quoteId'));
    const vehicleId = Number(this.route.snapshot.queryParamMap.get('vehicleId'));

    this.appointmentId = Number.isInteger(appointmentId) && appointmentId > 0 ? appointmentId : null;
    this.quoteId = Number.isInteger(quoteId) && quoteId > 0 ? quoteId : null;

    this.form.patchValue({
      performedAt: new Date().toISOString().slice(0, 10),
    });

    if (Number.isInteger(vehicleId) && vehicleId > 0) {
      this.form.controls.vehicleId.setValue(vehicleId);
    }

    if (this.quoteId) {
      this.loadQuote(this.quoteId);
    }
  }

  /** Precarga vehículo e ítems desde el presupuesto de origen, sin tocarlo. */
  private loadQuote(quoteId: number): void {
    this.loadingQuote.set(true);

    this.quoteService.findById(quoteId).subscribe({
      next: (quote) => {
        this.form.controls.vehicleId.setValue(quote.vehicleId);
        this.selectedVehicleLabel.set(`${quote.vehicleLicensePlate} · ${quote.vehicleDescription}`);
        this.items.clear();
        quote.items.forEach((item) =>
          this.items.push(
            this.createItem({
              serviceTypeId: item.serviceTypeId,
              supplyId: item.supplyId,
              description: item.description,
              quantity: item.quantity,
              unitPrice: item.unitPrice,
            }),
          ),
        );
        this.loadingQuote.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.loadingQuote.set(false);
        this.errorMessage.set(
          error.error?.message ?? 'No se pudo cargar el presupuesto de origen',
        );
      },
    });
  }

  /** Vino un vehicleId por query sin presupuesto de origen (p. ej. desde un turno): le pone etiqueta. */
  private prefillVehicleLabelIfMissing(vehicles: Vehicle[]): void {
    const vehicleId = this.form.controls.vehicleId.value;
    if (!vehicleId || this.selectedVehicleLabel()) {
      return;
    }

    const vehicle = vehicles.find((candidate) => candidate.id === vehicleId);
    if (vehicle) {
      this.selectedVehicleLabel.set(this.formatVehicle(vehicle));
    }
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
