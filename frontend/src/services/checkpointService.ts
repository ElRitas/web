import apiClient from './apiClient';
import type { CheckpointResponse, PassCheckpointResponse } from '../models/checkpoint';

class CheckpointService {
  async getCheckpoints(page: number = 1, count: number = 5): Promise<CheckpointResponse> {
    const response = await apiClient.get<CheckpointResponse>(`/checkpoints?pagesNum=${page}&elemCount=${count}`);
    return response.data;
  }

  async passCheckpoint(checkpointId: string): Promise<PassCheckpointResponse> {
    const response = await apiClient.post<{ passed: boolean }>(`/checkpoints/${checkpointId}/pass`);
    return response.data;
  }
}

export const checkpointService = new CheckpointService();