import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { HistoryService } from './history-service';

describe('HistoryService', () => {
  let service: HistoryService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(HistoryService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('requests the history of a customer', () => {
    service.findByCustomer(5).subscribe((result) => expect(result).toEqual([]));

    const request = httpMock.expectOne(
      (req) => req.url === `${environment.apiUrl}/work-orders/history`,
    );
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('customerId')).toBe('5');
    expect(request.request.params.has('vehicleId')).toBe(false);
    request.flush([]);
  });

  it('requests the history of a vehicle', () => {
    service.findByVehicle(9).subscribe((result) => expect(result).toEqual([]));

    const request = httpMock.expectOne(
      (req) => req.url === `${environment.apiUrl}/work-orders/history`,
    );
    expect(request.request.params.get('vehicleId')).toBe('9');
    expect(request.request.params.has('customerId')).toBe(false);
    request.flush([]);
  });
});
