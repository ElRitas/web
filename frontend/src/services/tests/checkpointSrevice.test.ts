import { describe, it, expect, vi, beforeEach } from 'vitest';
import { checkpointService } from '../checkpointService';

vi.mock('../apiClient', () => {
  const mockApiClient = {
    get: vi.fn(),
    post: vi.fn(),
  };
  return { default: mockApiClient };
});

import apiClient from '../apiClient';

describe('CheckpointService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getCheckpoints', () => {
    it('должен получать КПП с пагинацией по умолчанию', async () => {
      const mockResponse = {
        data: {
          checkpoints: [
            { id: '1', guardFio: 'John Doe', guardPhone: '+123456789', number: 'CP-1', status: 'OK' },
            { id: '2', guardFio: 'Jane Smith', guardPhone: '+987654321', number: 'CP-2', status: 'OUT_OF_ORDER' }
          ],
          pagination: { page: 1, total: 2, pages: 1 }
        }
      };

      (apiClient.get as any).mockResolvedValue(mockResponse);

      const result = await checkpointService.getCheckpoints();

      expect(apiClient.get).toHaveBeenCalledWith('/checkpoints?pagesNum=1&elemCount=5');
      expect(result).toEqual(mockResponse.data);
    });

    it('должен получать КПП с кастомной пагинацией', async () => {
      const mockResponse = {
        data: {
          checkpoints: [{ id: '1', guardFio: 'John Doe', guardPhone: '+123456789', number: 'CP-1', status: 'OK' }],
          pagination: { page: 2, total: 1, pages: 2 }
        }
      };

      (apiClient.get as any).mockResolvedValue(mockResponse);

      const result = await checkpointService.getCheckpoints(2, 10);

      expect(apiClient.get).toHaveBeenCalledWith('/checkpoints?pagesNum=2&elemCount=10');
      expect(result).toEqual(mockResponse.data);
    });

    it('должен обрабатывать ошибку получения КПП', async () => {
      (apiClient.get as any).mockRejectedValue(new Error('Get checkpoints failed'));

      await expect(checkpointService.getCheckpoints()).rejects.toThrow('Get checkpoints failed');
    });
  });

  describe('passCheckpoint', () => {
    it('должен проходить через КПП и возвращать результат', async () => {
      const checkpointId = 'checkpoint-123';
      const mockResponse = {
        data: { passed: true }
      };

      (apiClient.post as any).mockResolvedValue(mockResponse);

      const result = await checkpointService.passCheckpoint(checkpointId);

      expect(apiClient.post).toHaveBeenCalledWith('/checkpoints/checkpoint-123/pass');
      expect(result).toEqual({ passed: true });
    });

    it('должен обрабатывать случай когда не удалось пройти КПП', async () => {
      const checkpointId = 'checkpoint-123';
      const mockResponse = {
        data: { passed: false }
      };

      (apiClient.post as any).mockResolvedValue(mockResponse);

      const result = await checkpointService.passCheckpoint(checkpointId);

      expect(result).toEqual({ passed: false });
    });

    it('должен обрабатывать ошибку прохождения КПП', async () => {
      const checkpointId = 'checkpoint-123';
      (apiClient.post as any).mockRejectedValue(new Error('Pass checkpoint failed'));

      await expect(checkpointService.passCheckpoint(checkpointId)).rejects.toThrow('Pass checkpoint failed');
    });
  });
});