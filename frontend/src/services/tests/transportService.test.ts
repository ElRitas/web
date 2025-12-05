import { describe, it, expect, vi, beforeEach } from 'vitest';
import { transportService } from '../transportService';

vi.mock('../apiClient', () => {
  const mockApiClient = {
    post: vi.fn(),
    get: vi.fn(),
    delete: vi.fn(),
  };
  return { default: mockApiClient };
});

import apiClient from '../apiClient';

describe('TransportService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('addTransport', () => {
    it('должен добавлять транспорт', async () => {
      const transportData = {
        number: 'A123BC',
        model: 'Toyota Camry', 
        color: 'Black',
        insurance: true
      };

      (apiClient.post as any).mockResolvedValue({ data: {} });

      await transportService.addTransport(transportData);

      expect(apiClient.post).toHaveBeenCalledWith('/transport', transportData);
    });

    it('должен обрабатывать ошибку добавления транспорта', async () => {
      const transportData = {
        number: 'A123BC',
        model: 'Toyota Camry',
        color: 'Black', 
        insurance: true
      };

      (apiClient.post as any).mockRejectedValue(new Error('Add transport failed'));

      await expect(transportService.addTransport(transportData)).rejects.toThrow('Add transport failed');
    });
  });

  describe('getTransports', () => {
    it('должен получать транспорт с пагинацией по умолчанию', async () => {
      const mockResponse = {
        data: {
          transport: [
            { id: '1', number: 'A123BC', model: 'Toyota', color: 'Black', insurance: true },
            { id: '2', number: 'B456DE', model: 'Honda', color: 'White', insurance: false }
          ],
          pagination: { page: 1, total: 2, pages: 1 }
        }
      };

      (apiClient.get as any).mockResolvedValue(mockResponse);

      const result = await transportService.getTransports();

      expect(apiClient.get).toHaveBeenCalledWith('/transport?pagesNum=1&elemCount=3');
      expect(result).toEqual(mockResponse.data);
    });

    it('должен получать транспорт с кастомной пагинацией', async () => {
      const mockResponse = {
        data: {
          transport: [{ id: '1', number: 'A123BC', model: 'Toyota', color: 'Black', insurance: true }],
          pagination: { page: 2, total: 1, pages: 2 }
        }
      };

      (apiClient.get as any).mockResolvedValue(mockResponse);

      const result = await transportService.getTransports(2, 5);

      expect(apiClient.get).toHaveBeenCalledWith('/transport?pagesNum=2&elemCount=5');
      expect(result).toEqual(mockResponse.data);
    });
  });

  describe('deleteTransport', () => {
    it('должен удалять транспорт', async () => {
      const transportId = 'transport-123';

      (apiClient.delete as any).mockResolvedValue({ data: {} });

      await transportService.deleteTransport(transportId);

      expect(apiClient.delete).toHaveBeenCalledWith('/transport/transport-123');
    });
  });
});