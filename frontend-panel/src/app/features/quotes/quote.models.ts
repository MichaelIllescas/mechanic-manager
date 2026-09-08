export type QuoteStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface QuoteItemRequest {
  serviceTypeId: number | null;
  supplyId: number | null;
  description: string;
  quantity: number;
  unitPrice: number;
}

export interface CreateQuoteRequest {
  customerId: number;
  vehicleId: number;
  validUntil: string | null;
  notes: string | null;
  items: QuoteItemRequest[];
}

export type UpdateQuoteRequest = CreateQuoteRequest;

export interface UpdateQuoteStatusRequest {
  status: QuoteStatus;
}

export interface QuoteItem {
  id: number;
  serviceTypeId: number | null;
  supplyId: number | null;
  description: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export interface Quote {
  id: number;
  number: number;
  customerId: number;
  customerName: string;
  vehicleId: number;
  vehicleLicensePlate: string;
  vehicleDescription: string;
  validUntil: string | null;
  status: QuoteStatus;
  total: number;
  notes: string | null;
  items: QuoteItem[];
  createdAt: string;
  updatedAt: string;
}

export interface QuoteSummary {
  id: number;
  number: number;
  customerId: number;
  customerName: string;
  vehicleId: number;
  vehicleLicensePlate: string;
  validUntil: string | null;
  status: QuoteStatus;
  total: number;
  createdAt: string;
}

export interface QuotePage {
  content: QuoteSummary[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
