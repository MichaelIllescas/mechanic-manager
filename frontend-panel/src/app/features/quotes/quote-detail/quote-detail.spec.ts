import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { NotificationService } from '../../../core/notifications/notification-service';
import { QuoteService } from '../quote-service';
import { Quote } from '../quote.models';
import { QuoteDetail } from './quote-detail';

describe('QuoteDetail', () => {
  let component: QuoteDetail;
  let fixture: ComponentFixture<QuoteDetail>;
  let routeId: string | null;

  const quote: Quote = {
    id: 15,
    number: 24,
    customerId: 3,
    customerName: 'Ana Gómez',
    customerDocumentNumber: null,
    customerPhone: '1100000000',
    vehicleId: 8,
    vehicleLicensePlate: 'AB123CD',
    vehicleDescription: 'Toyota Corolla',
    validUntil: '2026-09-30',
    status: 'PENDING',
    total: 126500,
    notes: 'Valores sujetos a revisión',
    hasWorkOrder: false,
    items: [
      {
        id: 31,
        serviceTypeId: 5,
        supplyId: null,
        description: 'Cambio de distribución',
        quantity: 1,
        unitPrice: 125000,
        subtotal: 125000,
      },
      {
        id: 32,
        serviceTypeId: null,
        supplyId: 9,
        description: 'Refrigerante',
        quantity: 1,
        unitPrice: 1500,
        subtotal: 1500,
      },
    ],
    createdAt: '2026-09-07T10:00:00',
    updatedAt: '2026-09-07T10:00:00',
  };

  const quoteService = {
    findById: vi.fn().mockReturnValue(of(quote)),
    updateStatus: vi.fn().mockReturnValue(of({ ...quote, status: 'APPROVED' })),
  };
  const notifications = {
    success: vi.fn(),
  };

  beforeEach(async () => {
    routeId = '15';
    quoteService.findById.mockReset();
    quoteService.findById.mockReturnValue(of(quote));
    quoteService.updateStatus.mockReset();
    quoteService.updateStatus.mockReturnValue(of({ ...quote, status: 'APPROVED' }));
    notifications.success.mockReset();

    await TestBed.configureTestingModule({
      imports: [QuoteDetail],
      providers: [
        provideRouter([]),
        { provide: QuoteService, useValue: quoteService },
        { provide: NotificationService, useValue: notifications },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: () => routeId,
              },
            },
          },
        },
      ],
    }).compileComponents();
  });

  function createComponent(): void {
    fixture = TestBed.createComponent(QuoteDetail);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('loads the requested quote', () => {
    createComponent();

    expect(component).toBeTruthy();
    expect(quoteService.findById).toHaveBeenCalledWith(15);
    expect(component.quote()).toEqual(quote);
  });

  it('shows the customer phone', () => {
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Tel: 1100000000');
  });

  it('rejects an invalid route identifier without requesting data', () => {
    routeId = 'invalid';
    createComponent();

    expect(quoteService.findById).not.toHaveBeenCalled();
    expect(component.errorMessage()).toBe('El identificador del presupuesto no es válido');
  });

  it('changes the status using one of the three fixed actions', () => {
    createComponent();

    component.updateStatus('APPROVED');

    expect(quoteService.updateStatus).toHaveBeenCalledWith(15, { status: 'APPROVED' });
    expect(component.quote()?.status).toBe('APPROVED');
    expect(notifications.success).toHaveBeenCalledWith('Presupuesto marcado como aprobado');
  });

  it('does not request the status already assigned to the quote', () => {
    createComponent();

    component.updateStatus('PENDING');

    expect(quoteService.updateStatus).not.toHaveBeenCalled();
  });

  it('locks the status buttons when the quote already has a work order', () => {
    quoteService.findById.mockReturnValue(of({ ...quote, hasWorkOrder: true }));
    createComponent();

    const root = fixture.nativeElement as HTMLElement;
    const buttons = Array.from(root.querySelectorAll<HTMLButtonElement>('.status-actions button'));

    expect(buttons).toHaveLength(3);
    expect(buttons.every((button) => button.disabled)).toBe(true);
    expect(root.textContent).toContain('ya tiene un trabajo realizado');
  });

  it('opens the native print dialog to download the quote as PDF', () => {
    const print = vi.spyOn(window, 'print').mockImplementation(() => undefined);
    createComponent();

    component.downloadQuote();

    expect(print).toHaveBeenCalledOnce();
    print.mockRestore();
  });

  it('translates all quote statuses', () => {
    createComponent();

    expect(component.statusLabel('PENDING')).toBe('Pendiente');
    expect(component.statusLabel('APPROVED')).toBe('Aprobado');
    expect(component.statusLabel('REJECTED')).toBe('Rechazado');
  });
});
