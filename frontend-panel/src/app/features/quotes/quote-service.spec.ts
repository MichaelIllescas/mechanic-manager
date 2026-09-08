import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { QuoteService } from './quote-service';
import {
  CreateQuoteRequest,
  Quote,
  QuotePage,
  UpdateQuoteRequest,
} from './quote.models';

describe('QuoteService', () => {
  let service: QuoteService;
  let httpMock: HttpTestingController;

  const quote: Quote = {
    id: 15,
    number: 24,
    customerId: 3,
    customerName: 'Ana Gómez',
    vehicleId: 8,
    vehicleLicensePlate: 'AB123CD',
    vehicleDescription: 'Toyota Corolla',
    validUntil: '2026-09-30',
    status: 'PENDING',
    total: 125000,
    notes: 'Valores sujetos a revisión',
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
    ],
    createdAt: '2026-09-07T10:00:00',
    updatedAt: '2026-09-07T10:00:00',
  };

  const requestBody: CreateQuoteRequest = {
    customerId: 3,
    vehicleId: 8,
    validUntil: '2026-09-30',
    notes: 'Valores sujetos a revisión',
    items: [
      {
        serviceTypeId: 5,
        supplyId: null,
        description: 'Cambio de distribución',
        quantity: 1,
        unitPrice: 125000,
      },
    ],
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(QuoteService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should search quotes with trimmed filters and pagination', () => {
    const page: QuotePage = {
      content: [],
      page: 2,
      size: 10,
      totalElements: 0,
      totalPages: 0,
    };

    service.search('  Ana  ', 'APPROVED', 2, 10).subscribe((result) => {
      expect(result).toEqual(page);
    });

    const request = httpMock.expectOne(
      (candidate) =>
        candidate.url === `${environment.apiUrl}/quotes` &&
        candidate.params.get('search') === 'Ana' &&
        candidate.params.get('status') === 'APPROVED' &&
        candidate.params.get('page') === '2' &&
        candidate.params.get('size') === '10',
    );
    expect(request.request.method).toBe('GET');
    request.flush(page);
  });

  it('should omit empty optional search filters', () => {
    service.search('   ').subscribe();

    const request = httpMock.expectOne(
      (candidate) =>
        candidate.url === `${environment.apiUrl}/quotes` &&
        !candidate.params.has('search') &&
        !candidate.params.has('status'),
    );
    expect(request.request.params.get('page')).toBe('0');
    expect(request.request.params.get('size')).toBe('20');
    request.flush({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 });
  });

  it('should find a quote by id', () => {
    service.findById(15).subscribe((result) => expect(result).toEqual(quote));

    const request = httpMock.expectOne(`${environment.apiUrl}/quotes/15`);
    expect(request.request.method).toBe('GET');
    request.flush(quote);
  });

  it('should create a quote', () => {
    service.create(requestBody).subscribe((result) => expect(result).toEqual(quote));

    const request = httpMock.expectOne(`${environment.apiUrl}/quotes`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(requestBody);
    request.flush(quote);
  });

  it('should update a quote regardless of its current status', () => {
    const updateBody: UpdateQuoteRequest = {
      ...requestBody,
      notes: 'Corrección solicitada',
    };

    service.update(15, updateBody).subscribe((result) => expect(result).toEqual(quote));

    const request = httpMock.expectOne(`${environment.apiUrl}/quotes/15`);
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(updateBody);
    request.flush(quote);
  });

  it('should update the quote status', () => {
    service.updateStatus(15, { status: 'REJECTED' }).subscribe();

    const request = httpMock.expectOne(`${environment.apiUrl}/quotes/15/status`);
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({ status: 'REJECTED' });
    request.flush({ ...quote, status: 'REJECTED' });
  });
});
