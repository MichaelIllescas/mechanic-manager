export type DashboardAppointmentStatus = 'SCHEDULED' | 'COMPLETED' | 'CANCELLED';

export interface DashboardMetrics {
  todayAppointments: number;
  pendingQuotes: number;
  completedWorkOrdersThisMonth: number;
  registeredVehicles: number;
}

export interface DashboardAlerts {
  expiredPendingQuotes: number;
}

export interface DashboardAppointment {
  id: number;
  scheduledAt: string;
  status: DashboardAppointmentStatus;
  licensePlate: string;
  vehicleDescription: string;
  customerName: string;
}

export interface DashboardSummary {
  generatedAt: string;
  metrics: DashboardMetrics;
  alerts: DashboardAlerts;
  todayAppointments: DashboardAppointment[];
}
