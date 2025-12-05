export type UserRole = 'RESIDENT' | 'GUEST' | 'ADMIN';

export interface User {
  id: string;
  fio: string;
  email: string;
  password: string;
  residentialId: string;
  role: UserRole;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  address: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AddGuestRequest {
  fio: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
}

export interface ApiError {
  message: string;
  statusCode: number;
  error?: string;
}