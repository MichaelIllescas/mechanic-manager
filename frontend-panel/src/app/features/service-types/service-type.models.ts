export interface ServiceType {
  id: number;
  name: string;
  description: string | null;
  basePrice: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateServiceTypeRequest {
  name: string;
  description: string | null;
  basePrice: number;
}

export interface UpdateServiceTypeRequest {
  name: string;
  description: string | null;
  basePrice: number;
}

export interface UpdateServiceTypeStatusRequest {
  active: boolean;
}
