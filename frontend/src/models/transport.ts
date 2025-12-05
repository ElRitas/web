import { type Pagination } from "./api";

export interface Transport {
  id: string;
  number: string;
  model: string;
  color: string;
  insurance: boolean;
}

export interface UserTransport {
  id: string;
  userId: string;
  transportId: string;
}

export interface TransportResponse {
  transport: Transport[];
  pagination: Pagination;
}

export interface AddTransportRequest {
  number: string;
  model: string;
  color: string;
  insurance: boolean;
}

export interface TransportApiError {
  message: string;
  statusCode: number;
  error?: string;
}