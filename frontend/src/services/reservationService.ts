import apiClient from './apiClient';
import type {
  ReservationResponse, 
  AddReservationRequest, 
  UpdateReservationStatusRequest
} from '../models/reservation';

class ReservationService {
  async addReservation(reservationData: AddReservationRequest): Promise<void> {
    await apiClient.post('/reservations', reservationData);
  }

  async getReservations(page: number = 1, count: number = 10): Promise<ReservationResponse> {
    const response = await apiClient.get<ReservationResponse>(`/reservations?pagesNum=${page}&elemCount=${count}`);
    return response.data;
  }

  async deleteReservation(reservationId: string): Promise<void> {
    await apiClient.delete(`/reservations/${reservationId}`);
  }

  async updateReservationStatus(reservationId: string, statusData: UpdateReservationStatusRequest): Promise<void> {
    await apiClient.patch(`/reservations/${reservationId}/status`, statusData);
  }
}

export const reservationService = new ReservationService();