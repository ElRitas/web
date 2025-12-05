import { describe, it, expect, vi, beforeEach } from 'vitest';
import { slotService } from '../slotService';

vi.mock('../apiClient', () => {
  const mockApiClient = {
    post: vi.fn(),
    get: vi.fn(),
  };
  return { default: mockApiClient };
});

import apiClient from '../apiClient';

describe('SlotService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('addSlot', () => {
    it('должен добавлять слот и возвращать его ID', async () => {
      const slotData = {
        residentialId: 'res-123',
        slotNumber: 101
      };

      const mockResponse = {
        data: { slotId: 'slot-123' }
      };

      (apiClient.post as any).mockResolvedValue(mockResponse);

      const result = await slotService.addSlot(slotData);

      expect(apiClient.post).toHaveBeenCalledWith('/slots', slotData);
      expect(result).toEqual({ slotId: 'slot-123' });
    });

    it('должен обрабатывать ошибку добавления слота', async () => {
      const slotData = {
        residentialId: 'res-123',
        slotNumber: 101
      };

      (apiClient.post as any).mockRejectedValue(new Error('Add slot failed'));

      await expect(slotService.addSlot(slotData)).rejects.toThrow('Add slot failed');
    });
  });

  describe('getSlots', () => {
    it('должен получать слоты с пагинацией по умолчанию', async () => {
      const mockResponse = {
        data: {
          slots: [
            { id: '1', number: 101, status: 'FREE' },
            { id: '2', number: 102, status: 'OCCUPIED' }
          ],
          pagination: { page: 1, total: 2, pages: 1 }
        }
      };

      (apiClient.get as any).mockResolvedValue(mockResponse);

      const result = await slotService.getSlots();

      expect(apiClient.get).toHaveBeenCalledWith('/slots?pagesNum=1&elemCount=3');
      expect(result).toEqual(mockResponse.data);
    });

    it('должен получать слоты с кастомной пагинацией', async () => {
      const mockResponse = {
        data: {
          slots: [{ id: '1', number: 101, status: 'FREE' }],
          pagination: { page: 2, total: 1, pages: 2 }
        }
      };

      (apiClient.get as any).mockResolvedValue(mockResponse);

      const result = await slotService.getSlots(2, 5);

      expect(apiClient.get).toHaveBeenCalledWith('/slots?pagesNum=2&elemCount=5');
      expect(result).toEqual(mockResponse.data);
    });

    it('должен обрабатывать ошибку получения слотов', async () => {
      (apiClient.get as any).mockRejectedValue(new Error('Get slots failed'));

      await expect(slotService.getSlots()).rejects.toThrow('Get slots failed');
    });
  });
});