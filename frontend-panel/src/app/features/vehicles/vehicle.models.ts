export type VehicleType =
  | 'CAR'
  | 'SUV'
  | 'PICKUP'
  | 'VAN'
  | 'TRUCK'
  | 'MOTORCYCLE'
  | 'OTHER';

export interface Vehicle {
  id: number;
  customerId: number;
  customerFirstName: string;
  customerLastName: string;
  licensePlate: string;
  brand: string;
  model: string;
  year: number | null;
  chassisNumber: string | null;
  engineNumber: string | null;
  mileage: number | null;
  vehicleType: VehicleType;
  notes: string | null;
  createdAt: string | null;
  updatedAt: string | null;
}

export interface CreateVehicleRequest {
  customerId: number;
  licensePlate: string;
  brand: string;
  model: string;
  year: number | null;
  chassisNumber: string | null;
  engineNumber: string | null;
  mileage: number | null;
  vehicleType: VehicleType;
  notes: string | null;
}

export interface UpdateVehicleRequest {
  customerId: number;
  licensePlate: string;
  brand: string;
  model: string;
  year: number | null;
  chassisNumber: string | null;
  engineNumber: string | null;
  mileage: number | null;
  vehicleType: VehicleType;
  notes: string | null;
}
