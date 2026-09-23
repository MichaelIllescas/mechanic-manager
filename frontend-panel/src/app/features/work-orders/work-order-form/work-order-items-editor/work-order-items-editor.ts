import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  OnInit,
  computed,
  inject,
  input,
  signal,
  WritableSignal,
} from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import {
  FormArray,
  FormBuilder,
  FormControl,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

import { ServiceQuickCreate } from '../../../quotes/quote-form/quick-create/service-quick-create/service-quick-create';
import { SupplyQuickCreate } from '../../../quotes/quote-form/quick-create/supply-quick-create/supply-quick-create';
import { ServiceTypeService } from '../../../service-types/service-type-service';
import { ServiceType } from '../../../service-types/service-type.models';
import { SupplyService } from '../../../supplies/supply-service';
import { Supply } from '../../../supplies/supply.models';
import { WorkOrderItemRequest } from '../../work-order.models';

@Component({
  selector: 'app-work-order-items-editor',
  imports: [ReactiveFormsModule, ServiceQuickCreate, SupplyQuickCreate],
  templateUrl: './work-order-items-editor.html',
  styleUrl: './work-order-items-editor.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkOrderItemsEditor implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly servicesApi = inject(ServiceTypeService);
  private readonly suppliesApi = inject(SupplyService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly services = signal<ServiceType[]>([]);
  private readonly supplies = signal<Supply[]>([]);

  readonly items = input.required<FormArray>();
  readonly serviceSearch = new FormControl('', { nonNullable: true });
  readonly supplySearch = new FormControl('', { nonNullable: true });
  private readonly serviceQuery = toSignal(this.serviceSearch.valueChanges, { initialValue: '' });
  private readonly supplyQuery = toSignal(this.supplySearch.valueChanges, { initialValue: '' });
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly serviceResultsOpen = signal(false);
  readonly supplyResultsOpen = signal(false);
  readonly creationMode = signal<'service' | 'supply' | null>(null);
  readonly total = signal(0);
  readonly serviceResults = computed(() =>
    this.filter(
      this.services(),
      this.serviceQuery(),
      (item) => `${item.name} ${item.description ?? ''}`,
    ),
  );
  readonly supplyResults = computed(() =>
    this.filter(this.supplies(), this.supplyQuery(), (item) => item.name),
  );

  ngOnInit(): void {
    this.items()
      .valueChanges.pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.calculateTotal());
    this.calculateTotal();
    this.loadCatalogs();
  }

  addService(service: ServiceType): void {
    this.addOrIncrement('serviceTypeId', service.id, {
      serviceTypeId: service.id,
      supplyId: null,
      description: service.name,
      quantity: 1,
      unitPrice: service.basePrice,
    });
    this.serviceSearch.setValue('');
    this.serviceResultsOpen.set(false);
  }

  addSupply(supply: Supply): void {
    this.addOrIncrement('supplyId', supply.id, {
      serviceTypeId: null,
      supplyId: supply.id,
      description: supply.name,
      quantity: 1,
      unitPrice: supply.price,
    });
    this.supplySearch.setValue('');
    this.supplyResultsOpen.set(false);
  }

  serviceCreated(service: ServiceType): void {
    this.creationMode.set(null);
    this.services.update((items) => [service, ...items]);
    this.addService(service);
  }

  supplyCreated(supply: Supply): void {
    this.creationMode.set(null);
    this.supplies.update((items) => [supply, ...items]);
    this.addSupply(supply);
  }

  /** Cierra el desplegable cuando el foco sale del autocomplete (click afuera o Tab). */
  closeOnFocusOut(event: FocusEvent, open: WritableSignal<boolean>): void {
    const container = event.currentTarget as HTMLElement;
    if (!container.contains(event.relatedTarget as Node | null)) open.set(false);
  }

  addManualItem(): void {
    this.items().push(this.createItemGroup());
  }
  removeItem(index: number): void {
    this.items().removeAt(index);
  }

  isManualItem(item: FormArray['controls'][number]): boolean {
    return !item.get('serviceTypeId')?.value && !item.get('supplyId')?.value;
  }

  private loadCatalogs(): void {
    this.loading.set(true);
    let pending = 2;
    const done = () => {
      if (--pending === 0) this.loading.set(false);
    };
    this.servicesApi.findAll(true).subscribe({
      next: (items) => {
        this.services.set(items);
        done();
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage.set(error.error?.message ?? 'No se pudieron cargar los servicios');
        done();
      },
    });
    this.suppliesApi.findAll(true).subscribe({
      next: (items) => {
        this.supplies.set(items);
        done();
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage.set(error.error?.message ?? 'No se pudieron cargar los insumos');
        done();
      },
    });
  }

  private addOrIncrement(
    field: 'serviceTypeId' | 'supplyId',
    id: number,
    item: WorkOrderItemRequest,
  ): void {
    const existing = this.items().controls.find((control) => control.get(field)?.value === id);
    if (existing) {
      existing.get('quantity')?.setValue(Number(existing.get('quantity')?.value ?? 0) + 1);
      return;
    }
    this.items().push(this.createItemGroup(item));
  }

  private createItemGroup(item?: WorkOrderItemRequest) {
    return this.fb.group({
      serviceTypeId: [item?.serviceTypeId ?? (null as number | null)],
      supplyId: [item?.supplyId ?? (null as number | null)],
      description: [item?.description ?? '', [Validators.required, Validators.maxLength(255)]],
      quantity: [item?.quantity ?? 1, [Validators.required, Validators.min(0.01)]],
      unitPrice: [
        item?.unitPrice ?? 0,
        this.isCatalogItem(item) ? [Validators.required, Validators.min(0)] : [Validators.required],
      ],
    });
  }

  private isCatalogItem(item?: WorkOrderItemRequest): boolean {
    return Boolean(item?.serviceTypeId || item?.supplyId);
  }

  private calculateTotal(): void {
    this.total.set(
      this.items().controls.reduce(
        (sum, item) =>
          sum +
          Number(item.get('quantity')?.value ?? 0) * Number(item.get('unitPrice')?.value ?? 0),
        0,
      ),
    );
  }

  private filter<T>(items: T[], query: string, text: (item: T) => string): T[] {
    const normalized = query.trim().toLocaleLowerCase('es');
    return items
      .filter((item) => !normalized || text(item).toLocaleLowerCase('es').includes(normalized))
      .slice(0, 5);
  }
}
