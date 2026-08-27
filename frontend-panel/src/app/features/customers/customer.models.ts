export interface Customer {
  id: number;
  workshopId: number;
  firstName: string;
  lastName: string;
  documentNumber: string | null;
  phone: string;
  email: string | null;
  notes: string | null;
  createdAt: string | null;
  updatedAt: string | null;
}

export interface CreateCustomerRequest {
  firstName: string;
  lastName: string;
  documentNumber: string | null;
  phone: string;
  email: string | null;
  notes: string | null;
}

export interface UpdateCustomerRequest {
  firstName: string;
  lastName: string;
  documentNumber: string | null;
  phone: string;
  email: string | null;
  notes: string | null;
}
