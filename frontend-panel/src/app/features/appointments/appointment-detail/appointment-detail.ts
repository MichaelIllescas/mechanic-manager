import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, input, output, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { QuoteService } from '../../quotes/quote-service';
import { Quote } from '../../quotes/quote.models';
import { Appointment } from '../appointment.models';

/**
 * Panel lateral con el detalle de un turno.
 *
 * No conoce el calendario ni el servicio: recibe el turno y avisa qué quiso
 * hacer el usuario, para que la vista que lo abrió decida cómo resolverlo.
 */
@Component({
  selector: 'app-appointment-detail',
  imports: [RouterLink, CurrencyPipe, DatePipe],
  templateUrl: './appointment-detail.html',
  styleUrl: './appointment-detail.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    '(document:keydown.escape)': 'onEscape()',
  },
})
export class AppointmentDetail {
  private readonly quoteService = inject(QuoteService);

  readonly appointment = input.required<Appointment>();

  readonly closed = output<void>();
  readonly completed = output<Appointment>();
  readonly cancelled = output<Appointment>();

  readonly quotePreview = signal<Quote | null>(null);
  readonly loadingQuotePreview = signal(false);
  readonly quotePreviewError = signal<string | null>(null);

  openQuotePreview(quoteId: number): void {
    this.loadingQuotePreview.set(true);
    this.quotePreviewError.set(null);
    this.quotePreview.set(null);

    this.quoteService.findById(quoteId).subscribe({
      next: (quote) => {
        this.quotePreview.set(quote);
        this.loadingQuotePreview.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.loadingQuotePreview.set(false);
        this.quotePreviewError.set(error.error?.message ?? 'No se pudo cargar el presupuesto');
      },
    });
  }

  formatNumber(number: number): string {
    return number.toString().padStart(4, '0');
  }

  closeQuotePreview(): void {
    this.quotePreview.set(null);
    this.quotePreviewError.set(null);
  }

  onEscape(): void {
    if (this.quotePreview() || this.quotePreviewError()) {
      this.closeQuotePreview();
      return;
    }

    this.closed.emit();
  }
}
