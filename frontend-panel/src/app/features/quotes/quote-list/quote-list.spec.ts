import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { QuoteService } from '../quote-service';
import { QuotePage } from '../quote.models';
import { QuoteList } from './quote-list';

describe('QuoteList', () => {
  let component: QuoteList;
  let fixture: ComponentFixture<QuoteList>;

  const page: QuotePage = {
    content: [
      {
        id: 15,
        number: 24,
        customerId: 3,
        customerName: 'Ana Gómez',
        vehicleId: 8,
        vehicleLicensePlate: 'AB123CD',
        validUntil: '2026-09-30',
        status: 'PENDING',
        total: 125000,
        createdAt: '2026-09-07T10:00:00',
      },
    ],
    page: 0,
    size: 20,
    totalElements: 41,
    totalPages: 3,
  };

  const quoteService = {
    search: vi.fn().mockReturnValue(of(page)),
  };

  beforeEach(async () => {
    quoteService.search.mockReset();
    quoteService.search.mockReturnValue(of(page));

    await TestBed.configureTestingModule({
      imports: [QuoteList],
      providers: [provideRouter([]), { provide: QuoteService, useValue: quoteService }],
    }).compileComponents();

    fixture = TestBed.createComponent(QuoteList);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await waitForSearch();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads the first page without filters', () => {
    expect(quoteService.search).toHaveBeenCalledWith('', undefined, 0);
    expect(component.quotes()).toEqual(page.content);
    expect(component.totalElements()).toBe(41);
  });

  it('searches after the debounce time', async () => {
    component.searchControl.setValue('Ana');

    await waitForSearch();

    expect(quoteService.search).toHaveBeenLastCalledWith('Ana', undefined, 0);
  });

  it('filters by one of the fixed quote statuses', () => {
    component.filterByStatus('APPROVED');

    expect(component.selectedStatus()).toBe('APPROVED');
    expect(quoteService.search).toHaveBeenLastCalledWith('', 'APPROVED', 0);
  });

  it('requests the next page while preserving the active filters', () => {
    component.filterByStatus('REJECTED');
    component.nextPage();

    expect(quoteService.search).toHaveBeenLastCalledWith('', 'REJECTED', 1);
  });

  it('translates quote statuses', () => {
    expect(component.statusLabel('PENDING')).toBe('Pendiente');
    expect(component.statusLabel('APPROVED')).toBe('Aprobado');
    expect(component.statusLabel('REJECTED')).toBe('Rechazado');
  });

  function waitForSearch(): Promise<void> {
    return new Promise((resolve) => setTimeout(resolve, 350));
  }
});
