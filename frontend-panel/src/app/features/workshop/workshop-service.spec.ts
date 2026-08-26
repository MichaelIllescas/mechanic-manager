import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { environment } from '../../../environments/environment';
import { WorkshopService } from './workshop-service';

describe('WorkshopService', () => {
  let service: WorkshopService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(WorkshopService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get the authenticated user workshop', () => {
    service.findMine().subscribe();

    const request = httpMock.expectOne(`${environment.apiUrl}/workshops/me`);
    expect(request.request.method).toBe('GET');
    request.flush({});
  });

  it('should update the authenticated user workshop', () => {
    const body = {
      name: 'Taller Norte',
      phone: '123456',
      email: 'taller@example.com',
      address: 'Av. Siempre Viva 123',
      notes: null,
    };

    service.update(body).subscribe();

    const request = httpMock.expectOne(`${environment.apiUrl}/workshops/me`);
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(body);
    request.flush({});
  });
});
