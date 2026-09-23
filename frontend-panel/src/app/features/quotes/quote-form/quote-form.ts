import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { NotificationService } from '../../../core/notifications/notification-service';
import { QuoteService } from '../quote-service';
import { CreateQuoteRequest, QuoteItemRequest } from '../quote.models';
import { QuoteItemsEditor } from './quote-items-editor/quote-items-editor';
import { QuotePartyPicker } from './quote-party-picker/quote-party-picker';

@Component({
  selector: 'app-quote-form',
  imports: [ReactiveFormsModule, RouterLink, QuotePartyPicker, QuoteItemsEditor],
  templateUrl: './quote-form.html',
  styleUrl: './quote-form.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class QuoteForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly quotes = inject(QuoteService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly notifications = inject(NotificationService);
  private quoteId: number | null = null;

  readonly isEdit = signal(false);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly customerLabel = signal('');
  readonly vehicleLabel = signal('');
  readonly form = this.fb.group({
    customerId: [null as number | null, [Validators.required, Validators.min(1)]],
    vehicleId: [null as number | null, [Validators.required, Validators.min(1)]],
    validUntil: [''],
    notes: ['', Validators.maxLength(2000)],
    items: this.fb.array([], Validators.required),
  });

  get items(): FormArray {
    return this.form.controls.items;
  }

  ngOnInit(): void {
    const routeId = this.route.snapshot.paramMap.get('id');
    if (routeId === null) return;

    const id = Number(routeId);
    if (!Number.isInteger(id) || id <= 0) {
      this.errorMessage.set('El identificador del presupuesto no es válido');
      return;
    }
    this.quoteId = id;
    this.isEdit.set(true);
    this.load(id);
  }

  onSubmit(): void {
    if (this.form.invalid || this.items.length === 0 || this.loading()) {
      this.form.markAllAsTouched();
      if (this.items.length === 0) this.errorMessage.set('Agregá al menos un servicio o insumo');
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);
    const operation = this.isEdit()
      ? this.quotes.update(this.quoteId!, this.payload())
      : this.quotes.create(this.payload());

    operation.subscribe({
      next: (quote) => {
        this.notifications.success(
          this.isEdit()
            ? 'Presupuesto actualizado correctamente'
            : 'Presupuesto creado correctamente',
        );
        void this.router.navigate(['/quotes', quote.id]);
      },
      error: (error: HttpErrorResponse) => {
        this.saving.set(false);
        this.errorMessage.set(
          error.error?.message ??
            `No se pudo ${this.isEdit() ? 'actualizar' : 'crear'} el presupuesto`,
        );
      },
    });
  }

  private load(id: number): void {
    this.loading.set(true);
    this.quotes.findById(id).subscribe({
      next: (quote) => {
        this.form.patchValue({
          customerId: quote.customerId,
          vehicleId: quote.vehicleId,
          validUntil: quote.validUntil ?? '',
          notes: quote.notes ?? '',
        });
        this.customerLabel.set(quote.customerName);
        this.vehicleLabel.set(`${quote.vehicleLicensePlate} · ${quote.vehicleDescription}`);
        this.items.clear();
        quote.items.forEach((item) => this.items.push(this.createItem(item)));
        this.loading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);
        this.errorMessage.set(error.error?.message ?? 'No se pudo cargar el presupuesto');
      },
    });
  }

  private createItem(item: QuoteItemRequest) {
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

  private payload(): CreateQuoteRequest {
    const value = this.form.getRawValue();
    return {
      customerId: value.customerId!,
      vehicleId: value.vehicleId!,
      validUntil: value.validUntil || null,
      notes: value.notes?.trim() || null,
      items: this.items.controls.map((item) => ({
        serviceTypeId: item.get('serviceTypeId')?.value ?? null,
        supplyId: item.get('supplyId')?.value ?? null,
        description: String(item.get('description')?.value ?? '').trim(),
        quantity: Number(item.get('quantity')?.value),
        unitPrice: Number(item.get('unitPrice')?.value),
      })),
    };
  }
}
