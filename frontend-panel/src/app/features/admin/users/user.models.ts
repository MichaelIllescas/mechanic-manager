export interface User {
  id: number;
  name: string;
  email: string;
  role: 'SYSTEM_ADMIN' | 'USER';
  status: 'ENABLED' | 'DISABLED';
  mustChangePassword: boolean;
  createdAt: string;
}

export interface CreateUserRequest {
  name: string;
  email: string;
  password: string;
  role: 'SYSTEM_ADMIN' | 'USER';
}

export interface UpdateUserStatusRequest {
  status: 'ENABLED' | 'DISABLED';
}

export interface UpdateUserRequest {
  name: string;
  email: string;
  role: 'SYSTEM_ADMIN' | 'USER';
}
