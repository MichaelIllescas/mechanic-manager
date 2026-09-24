import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, input, output, signal } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { switchMap } from 'rxjs';

import { NotificationService } from '../../../../core/notifications/notification-service';
import { nonNegativeTotal } from '../../../../shared/utils/items-total';
import { QuoteItemsEditor } from '../../../quotes/quote-form/quote-items-editor/quote-items-editor';
import { QuoteService } from '../../../quotes/quote-service';
import { CreateQuoteRequest, Quote } from '../../../quotes/quote.models';

/**
 * Alta rápida de un presupuesto sin salir del alta de turno: se usa cuando el
 * vehículo elegido no tiene ninguno aprobado para vincular.
 */
@Component({
  selector: 'app-quote-quick-create',
  imports: [ReactiveFormsModule, QuoteItemsEditor],
  templateUrl: './quote-quick-create.html',
  styleUrl: './quote-quick-create.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class QuoteQuickCreate {
  private readonly fb = inject(FormBuilder);
  private readonly quoteService = inject(QuoteService);
  private readonly notifications = inject(NotificationService);
  private createdQuoteId: number | null = null;

  readonly vehicleId = input.required<number>();
  readonly customerId = input.required<number>();
  readonly vehicleLabel = input.required<string>();

  readonly saved = output<Quote>();
  readonly cancelled = output<void>();

  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.group({
    validUntil: [''],
    notes: ['', Validators.maxLength(2000)],
    items: this.fb.array([], [Validators.required, nonNegativeTotal]),
  });

  get items(): FormArray {
    return this.form.controls.items;
  }

  save(): void {
    if (this.form.invalid || this.items.length === 0 || this.saving()) {
      this.form.markAllAsTouched();
      if (this.items.length === 0) {
        this.errorMessage.set('Agregá al menos un servicio o insumo');
      } else if (this.items.hasError('negativeTotal')) {
        this.errorMessage.set('El descuento no puede superar el total del presupuesto');
      }
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);

    const operation = this.createdQuoteId
      ? this.quoteService.updateStatus(this.createdQuoteId, { status: 'APPROVED' })
      : this.quoteService.create(this.payload()).pipe(
          switchMap((created) => {
            this.createdQuoteId = created.id;
            return this.quoteService.updateStatus(created.id, { status: 'APPROVED' });
          }),
        );

    operation.subscribe({
      next: (approved) => {
        this.createdQuoteId = null;
        this.saving.set(false);
        this.notifications.success('Presupuesto creado y aprobado correctamente');
        this.saved.emit(approved);
      },
      error: (error: HttpErrorResponse) => {
        this.saving.set(false);
        this.errorMessage.set(
          error.error?.message ??
            (this.createdQuoteId
              ? 'El presupuesto se creó, pero no se pudo aprobar. Volvé a intentar.'
              : 'No se pudo crear el presupuesto'),
        );
      },
    });
  }

  cancel(): void {
    if (!this.saving()) {
      this.cancelled.emit();
    }
  }

  private payload(): CreateQuoteRequest {
    const value = this.form.getRawValue();
    return {
      customerId: this.customerId(),
      vehicleId: this.vehicleId(),
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
