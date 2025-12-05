export interface ApiError {
  message: string;
  statusCode: number;
  error?: string;
}

export interface Pagination {
  totalPages: number;
  curPage: number;
}