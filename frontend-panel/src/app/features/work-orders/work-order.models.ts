export type WorkOrderStatus = 'COMPLETED' | 'CANCELLED';

export interface WorkOrderItemRequest {
  serviceTypeId: number | null;
  supplyId: number | null;
  description: string;
  quantity: number;
  unitPrice: number;
}

export interface CreateWorkOrderRequest {
  vehicleId: number;
  appointmentId: number | null;
  quoteId: number | null;
  performedAt: string;
  mileage: number | null;
  notes: string | null;
  items: WorkOrderItemRequest[];
}

export interface UpdateWorkOrderRequest {
  performedAt: string;
  mileage: number | null;
  notes: string | null;
  items: WorkOrderItemRequest[];
}

export interface UpdateWorkOrderStatusRequest {
  status: WorkOrderStatus;
}

export interface WorkOrderItem {
  id: number;
  serviceTypeId: number | null;
  supplyId: number | null;
  description: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export interface WorkOrder {
  id: number;
  vehicleId: number;
  vehicleLicensePlate: string;
  vehicleDescription: string;
  customerId: number;
  customerName: string;
  appointmentId: number | null;
  quoteId: number | null;
  quoteNumber: number | null;
  performedAt: string;
  mileage: number | null;
  status: WorkOrderStatus;
  total: number;
  notes: string | null;
  items: WorkOrderItem[];
  createdAt: string;
  updatedAt: string;
}

export interface WorkOrderSummary {
  id: number;
  vehicleId: number;
  vehicleLicensePlate: string;
  customerId: number;
  customerName: string;
  performedAt: string;
  status: WorkOrderStatus;
  total: number;
}

export interface WorkOrderPage {
  content: WorkOrderSummary[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
