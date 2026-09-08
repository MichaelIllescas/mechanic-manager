import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  Subject,
  catchError,
  debounceTime,
  distinctUntilChanged,
  of,
  startWith,
  switchMap,
  tap,
} from 'rxjs';

import { QuoteService } from '../quote-service';
import { QuotePage, QuoteStatus, QuoteSummary } from '../quote.models';

interface QuoteSearchRequest {
  search: string;
  status?: QuoteStatus;
  page: number;
}

@Component({
  selector: 'app-quote-list',
  imports: [CurrencyPipe, DatePipe, ReactiveFormsModule, RouterLink],
  templateUrl: './quote-list.html',
  styleUrl: './quote-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class QuoteList implements OnInit {
  private readonly quoteService = inject(QuoteService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly requests = new Subject<QuoteSearchRequest>();

  readonly quotes = signal<QuoteSummary[]>([]);
  readonly selectedStatus = signal<QuoteStatus | null>(null);
  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly totalElements = signal(0);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly searchControl = new FormControl('', { nonNullable: true });

  ngOnInit(): void {
    this.requests
      .pipe(
        tap(() => {
          this.loading.set(true);
          this.errorMessage.set(null);
        }),
        switchMap(({ search, status, page }) =>
          this.quoteService.search(search, status, page).pipe(
            catchError((error: HttpErrorResponse) => {
              this.errorMessage.set(
                error.error?.message ?? 'No se pudieron cargar los presupuestos',
              );
              return of(this.emptyPage(page));
            }),
          ),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((result) => this.applyPage(result));

    this.searchControl.valueChanges
      .pipe(
        startWith(this.searchControl.value),
        debounceTime(300),
        distinctUntilChanged(),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => this.loadPage(0));
  }

  filterByStatus(status: QuoteStatus | null): void {
    if (this.selectedStatus() === status) {
      return;
    }

    this.selectedStatus.set(status);
    this.loadPage(0);
  }

  clearSearch(): void {
    this.searchControl.setValue('');
  }

  previousPage(): void {
    if (this.page() > 0) {
      this.loadPage(this.page() - 1);
    }
  }

  nextPage(): void {
    if (this.page() + 1 < this.totalPages()) {
      this.loadPage(this.page() + 1);
    }
  }

  statusLabel(status: QuoteStatus): string {
    const labels: Record<QuoteStatus, string> = {
      PENDING: 'Pendiente',
      APPROVED: 'Aprobado',
      REJECTED: 'Rechazado',
    };

    return labels[status];
  }

  private loadPage(page: number): void {
    this.requests.next({
      search: this.searchControl.value,
      status: this.selectedStatus() ?? undefined,
      page,
    });
  }

  private applyPage(result: QuotePage): void {
    this.quotes.set(result.content);
    this.page.set(result.page);
    this.totalPages.set(result.totalPages);
    this.totalElements.set(result.totalElements);
    this.loading.set(false);
  }

  private emptyPage(page: number): QuotePage {
    return {
      content: [],
      page,
      size: 20,
      totalElements: 0,
      totalPages: 0,
    };
  }
}
