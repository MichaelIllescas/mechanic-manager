export interface Supply {
  id: number;
  name: string;
  price: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateSupplyRequest {
  name: string;
  price: number;
}

export interface UpdateSupplyRequest {
  name: string;
  price: number;
}

export interface UpdateSupplyStatusRequest {
  active: boolean;
}
