import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { NotificationService } from '../../../core/notifications/notification-service';
import { QuoteService } from '../quote-service';
import { Quote, QuoteStatus } from '../quote.models';

@Component({
  selector: 'app-quote-detail',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './quote-detail.html',
  styleUrl: './quote-detail.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class QuoteDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly quoteService = inject(QuoteService);
  private readonly notifications = inject(NotificationService);
  private readonly changeDetector = inject(ChangeDetectorRef);

  readonly quote = signal<Quote | null>(null);
  readonly loading = signal(false);
  readonly updatingStatus = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly downloadedAt = signal<Date | null>(null);

  ngOnInit(): void {
    const quoteId = Number(this.route.snapshot.paramMap.get('id'));

    if (!Number.isInteger(quoteId) || quoteId <= 0) {
      this.errorMessage.set('El identificador del presupuesto no es válido');
      return;
    }

    this.loadQuote(quoteId);
  }

  updateStatus(status: QuoteStatus): void {
    const quote = this.quote();
    if (!quote || quote.status === status || this.updatingStatus()) {
      return;
    }

    this.updatingStatus.set(true);
    this.errorMessage.set(null);

    this.quoteService.updateStatus(quote.id, { status }).subscribe({
      next: (updated) => {
        this.quote.set(updated);
        this.updatingStatus.set(false);
        this.notifications.success(
          `Presupuesto marcado como ${this.statusLabel(status).toLowerCase()}`,
        );
      },
      error: (error: HttpErrorResponse) => {
        this.updatingStatus.set(false);
        this.errorMessage.set(error.error?.message ?? 'No se pudo actualizar el estado');
      },
    });
  }

  downloadQuote(): void {
    const previousTitle = document.title;

    this.downloadedAt.set(new Date());
    this.changeDetector.detectChanges();
    document.title = '';
    window.print();
    document.title = previousTitle;
  }

  statusLabel(status: QuoteStatus): string {
    const labels: Record<QuoteStatus, string> = {
      PENDING: 'Pendiente',
      APPROVED: 'Aprobado',
      REJECTED: 'Rechazado',
    };

    return labels[status];
  }

  localDateTime(value: string): Date {
    const hasTimeZone = /(?:Z|[+-]\d{2}:?\d{2})$/i.test(value);
    return new Date(hasTimeZone ? value : `${value}Z`);
  }

  private loadQuote(quoteId: number): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.quoteService.findById(quoteId).subscribe({
      next: (quote) => {
        this.quote.set(quote);
        this.loading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);
        this.errorMessage.set(error.error?.message ?? 'No se pudo cargar el presupuesto');
      },
    });
  }
}
