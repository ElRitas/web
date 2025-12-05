export * from './user';
export * from './transport';
export * from './slot';
export * from './reservation';
export * from './checkpoint';

// Базовые модели
export interface Pagination {
  totalPages: number;
  curPage: number;
}

export interface ApiResponse<T> {
  data: T;
  message?: string;
  status: string;
}

export interface ApiError {
  message: string;
  statusCode: number;
  error?: string;
}