export interface ServiceType {
  id: number;
  name: string;
  description: string | null;
  basePrice: number;
  estimatedMinutes: number | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateServiceTypeRequest {
  name: string;
  description: string | null;
  basePrice: number;
  estimatedMinutes: number | null;
}

export interface UpdateServiceTypeRequest {
  name: string;
  description: string | null;
  basePrice: number;
  estimatedMinutes: number | null;
}

export interface UpdateServiceTypeStatusRequest {
  active: boolean;
}
