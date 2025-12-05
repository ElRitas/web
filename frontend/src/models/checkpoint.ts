import { type Pagination } from "./api";

export type CheckpointStatus = 'OK' | 'OUT_OF_ORDER';

export interface Checkpoint {
  id: string;
  guardFio: string;
  guardPhone: string;
  number: string;
  status: CheckpointStatus;
}

export interface CheckpointResponse {
  checkpoints: Checkpoint[];
  pagination: Pagination;
}

export interface PassCheckpointResponse {
  passed: boolean;
}

export interface CheckpointApiError {
  message: string;
  statusCode: number;
  error?: string;
}