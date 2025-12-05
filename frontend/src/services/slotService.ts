import apiClient from './apiClient';
import type { SlotResponse, AddSlotRequest, AddSlotResponse } from '../models/slot';

class SlotService {
  async addSlot(slotData: AddSlotRequest): Promise<AddSlotResponse> {
    const response = await apiClient.post<{ slotId: string }>('/slots', slotData);
    return response.data;
  }

  async getSlots(page: number = 1, count: number = 3): Promise<SlotResponse> {
    const response = await apiClient.get<SlotResponse>(`/slots?pagesNum=${page}&elemCount=${count}`);
    return response.data;
  }
}

export const slotService = new SlotService();