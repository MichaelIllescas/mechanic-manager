import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  HostListener,
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

import { NotificationService } from '../../../core/notifications/notification-service';
import { ConfirmService } from '../../../core/confirm/confirm-service';
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
  private readonly notifications = inject(NotificationService);
  private readonly confirmService = inject(ConfirmService);
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
  readonly openMenuId = signal<number | null>(null);
  readonly menuPosition = signal<{ top?: number; bottom?: number; right: number } | null>(null);
  readonly updatingStatusId = signal<number | null>(null);
  readonly deletingId = signal<number | null>(null);

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

  toggleMenu(quoteId: number, event: MouseEvent): void {
    if (this.openMenuId() === quoteId) {
      this.closeMenu();
      return;
    }

    const button = event.currentTarget as HTMLElement;
    const rect = button.getBoundingClientRect();
    const dropdownHeight = 220;
    const right = window.innerWidth - rect.right;

    if (rect.bottom + 8 + dropdownHeight > window.innerHeight) {
      this.menuPosition.set({ bottom: window.innerHeight - rect.top + 8, right });
    } else {
      this.menuPosition.set({ top: rect.bottom + 8, right });
    }

    this.openMenuId.set(quoteId);
  }

  closeMenu(): void {
    this.openMenuId.set(null);
    this.menuPosition.set(null);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (this.openMenuId() === null) {
      return;
    }

    const target = event.target as HTMLElement;
    if (!target.closest('.status-menu')) {
      this.closeMenu();
    }
  }

  changeStatus(quote: QuoteSummary, status: QuoteStatus): void {
    this.closeMenu();

    if (quote.status === status || this.updatingStatusId()) {
      return;
    }

    this.updatingStatusId.set(quote.id);

    this.quoteService.updateStatus(quote.id, { status }).subscribe({
      next: (updated) => {
        this.quotes.update((list) =>
          list.map((item) => (item.id === updated.id ? { ...item, status: updated.status } : item)),
        );
        this.updatingStatusId.set(null);
        this.notifications.success(
          `Presupuesto marcado como ${this.statusLabel(status).toLowerCase()}`,
        );
      },
      error: (error: HttpErrorResponse) => {
        this.updatingStatusId.set(null);
        this.notifications.error(error.error?.message ?? 'No se pudo actualizar el estado');
      },
    });
  }

  async deleteQuote(quote: QuoteSummary): Promise<void> {
    this.closeMenu();

    if (this.deletingId()) {
      return;
    }

    const confirmed = await this.confirmService.confirm({
      title: 'Eliminar presupuesto',
      message: `¿Eliminar el presupuesto N.° ${quote.number} de ${quote.customerName}? Esta acción no se puede deshacer.`,
      confirmLabel: 'Eliminar',
      danger: true,
    });
    if (!confirmed) {
      return;
    }

    this.deletingId.set(quote.id);

    this.quoteService.delete(quote.id).subscribe({
      next: () => {
        this.deletingId.set(null);
        this.quotes.update((list) => list.filter((item) => item.id !== quote.id));
        this.totalElements.update((total) => Math.max(0, total - 1));
        this.notifications.success('Presupuesto eliminado correctamente');
      },
      error: (error: HttpErrorResponse) => {
        this.deletingId.set(null);
        this.notifications.error(error.error?.message ?? 'No se pudo eliminar el presupuesto');
      },
    });
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
