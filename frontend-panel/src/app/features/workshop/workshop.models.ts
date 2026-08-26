export interface Workshop {
  id: number;
  name: string | null;
  phone: string | null;
  email: string | null;
  address: string | null;
  notes: string | null;
  complete: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface UpdateWorkshopRequest {
  name: string | null;
  phone: string | null;
  email: string | null;
  address: string | null;
  notes: string | null;
}
