import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { DashboardService } from './dashboard-service';
import { DashboardSummary } from './dashboard.models';

describe('DashboardService', () => {
  let service: DashboardService;
  let httpMock: HttpTestingController;

  const summary: DashboardSummary = {
    generatedAt: '2026-09-23T10:30:00',
    metrics: {
      todayAppointments: 3,
      pendingQuotes: 2,
      completedWorkOrdersThisMonth: 8,
      registeredVehicles: 42,
    },
    alerts: { expiredPendingQuotes: 1 },
    todayAppointments: [],
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(DashboardService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('loads the dashboard summary from its aggregate endpoint', () => {
    service.getSummary().subscribe((result) => expect(result).toEqual(summary));

    const request = httpMock.expectOne(`${environment.apiUrl}/dashboard/summary`);
    expect(request.request.method).toBe('GET');
    request.flush(summary);
  });
});
