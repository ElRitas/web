import apiClient from './apiClient';
import type { AddTransportRequest, TransportResponse } from '../models/transport';

class TransportService {
  async addTransport(transportData: AddTransportRequest): Promise<void> {
    await apiClient.post('/transport', transportData);
  }

  async getTransports(page: number = 1, count: number = 3): Promise<TransportResponse> {
    const response = await apiClient.get<TransportResponse>(`/transport?pagesNum=${page}&elemCount=${count}`);
    return response.data;
  }

  async deleteTransport(transportId: string): Promise<void> {
    await apiClient.delete(`/transport/${transportId}`);
  }
}

export const transportService = new TransportService();