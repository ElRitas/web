import { type Pagination } from "./api";

export type SlotStatus = 'FREE' | 'OCCUPIED' | 'RESERVED';

export interface Slot {
  id: string;
  number: number;
  status: SlotStatus;
}

export interface SlotResponse {
  slots: Slot[];
  pagination: Pagination;
}

export interface AddSlotRequest {
  residentialId: string;
  slotNumber: number;
}

export interface AddSlotResponse {
  slotId: string;
}

export interface SlotApiError {
  message: string;
  statusCode: number;
  error?: string;
}