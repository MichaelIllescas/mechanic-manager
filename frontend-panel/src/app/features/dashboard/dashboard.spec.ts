import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { AuthService } from '../../core/auth/auth-service';
import { DashboardService } from './dashboard-service';
import { DashboardSummary } from './dashboard.models';
import { Dashboard } from './dashboard';

describe('Dashboard', () => {
  let fixture: ComponentFixture<Dashboard>;
  let component: Dashboard;

  const summary: DashboardSummary = {
    generatedAt: '2026-09-23T10:30:00',
    metrics: {
      todayAppointments: 1,
      pendingQuotes: 2,
      completedWorkOrdersThisMonth: 8,
      registeredVehicles: 42,
    },
    alerts: { expiredPendingQuotes: 1 },
    todayAppointments: [
      {
        id: 50,
        scheduledAt: '2026-09-23T11:00:00',
        status: 'SCHEDULED',
        licensePlate: 'AB123CD',
        vehicleDescription: 'Toyota Corolla',
        customerName: 'Ana Gómez',
      },
    ],
  };

  const dashboardService = { getSummary: vi.fn().mockReturnValue(of(summary)) };
  const authService = { getUserName: vi.fn().mockReturnValue('Joni') };

  beforeEach(async () => {
    dashboardService.getSummary.mockReset();
    dashboardService.getSummary.mockReturnValue(of(summary));

    await TestBed.configureTestingModule({
      imports: [Dashboard],
      providers: [
        provideRouter([]),
        { provide: DashboardService, useValue: dashboardService },
        { provide: AuthService, useValue: authService },
      ],
    }).compileComponents();
  });

  function createComponent(): void {
    fixture = TestBed.createComponent(Dashboard);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('loads and renders the operational summary', () => {
    createComponent();

    const text = fixture.nativeElement.textContent as string;
    expect(dashboardService.getSummary).toHaveBeenCalledOnce();
    expect(text).toContain('Buen día, Joni');
    expect(text).toContain('Vehículos registrados');
    expect(text).toContain('42');
    expect(text).toContain('AB123CD');
    expect(text).toContain('Ana Gómez');
    expect(text).toContain('Presupuestos pendientes vencidos');
    expect(fixture.nativeElement.querySelectorAll('.summary-card__icon')).toHaveLength(4);
  });

  it('refreshes the summary from an accessible icon button', () => {
    createComponent();

    const refreshButton = fixture.nativeElement.querySelector(
      'button[aria-label="Actualizar resumen"]',
    ) as HTMLButtonElement;

    expect(refreshButton).toBeTruthy();
    expect(refreshButton.querySelector('svg[lucideRefreshCw]')).toBeTruthy();

    refreshButton.click();

    expect(dashboardService.getSummary).toHaveBeenCalledTimes(2);
  });

  it('shows a retryable error when loading fails', () => {
    dashboardService.getSummary.mockReturnValue(
      throwError(() => ({ error: { message: 'No disponible' } })),
    );

    createComponent();

    expect(component.errorMessage()).toBe('No disponible');
    expect(fixture.nativeElement.textContent).toContain('Reintentar');
  });
});
