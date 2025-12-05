import { type Pagination } from "./api";

export type ReservationStatus = 'ACTIVE' | 'CANCELLED' | 'COMPLETED';

export interface Reservation {
  id: string;
  slotId: string;
  transportId: string;
  userId: string;
  status: ReservationStatus;
}

export interface ReservationInfo {
  id: string;
  number: number;
  transportNumber: string;
  transportModel: string;
  status?: ReservationStatus;
}

export interface ReservationResponse {
  reservations: ReservationInfo[];
  pagination: Pagination;
}

export interface AddReservationRequest {
  slotId: string;
  transportId: string;
}

export interface UpdateReservationStatusRequest {
  status: ReservationStatus;
}

export interface SlotWithStatus {
  id: string;
  number: number;
  status: 'FREE' | 'OCCUPIED_BY_ME' | 'OCCUPIED_BY_OTHER';
  currentReservationId?: string;
}

export interface ReservationApiError {
  message: string;
  statusCode: number;
  error?: string;
}