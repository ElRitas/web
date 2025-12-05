import { describe, it, expect, vi, beforeEach } from 'vitest';
import { reservationService } from '../reservationService';

vi.mock('../apiClient', () => {
  const mockApiClient = {
    post: vi.fn(),
    get: vi.fn(),
    delete: vi.fn(),
    patch: vi.fn(),
  };
  return { default: mockApiClient };
});

import apiClient from '../apiClient';

describe('ReservationService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('addReservation', () => {
    it('должен добавлять бронь', async () => {
      const reservationData = {
        slotId: 'slot-123',
        transportId: 'transport-456'
      };

      (apiClient.post as any).mockResolvedValue({ data: {} });

      await reservationService.addReservation(reservationData);

      expect(apiClient.post).toHaveBeenCalledWith('/reservations', reservationData);
    });
  });

  describe('getReservations', () => {
    it('должен получать брони', async () => {
      const mockResponse = {
        data: {
          reservations: [
            { id: '1', number: 101, transportNumber: 'A123BC', transportModel: 'Toyota' },
            { id: '2', number: 102, transportNumber: 'B456DE', transportModel: 'Honda' }
          ],
          pagination: { page: 1, total: 2, pages: 1 }
        }
      };

      (apiClient.get as any).mockResolvedValue(mockResponse);

      const result = await reservationService.getReservations();

      expect(apiClient.get).toHaveBeenCalledWith('/reservations?pagesNum=1&elemCount=10');
      expect(result).toEqual(mockResponse.data);
    });
  });

  describe('deleteReservation', () => {
    it('должен удалять бронь', async () => {
      const reservationId = 'reservation-123';

      (apiClient.delete as any).mockResolvedValue({ data: {} });

      await reservationService.deleteReservation(reservationId);

      expect(apiClient.delete).toHaveBeenCalledWith('/reservations/reservation-123');
    });
  });
});