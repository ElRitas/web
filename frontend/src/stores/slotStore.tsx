import { createContext, useContext, useReducer } from 'react';
import type { Slot, AddSlotRequest, AddSlotResponse } from '../models/slot';
import { slotService } from '../services/slotService';

interface SlotState {
  slots: Slot[];
  loading: boolean;
  error: string | null;
  lastAddedSlot: AddSlotResponse | null;
}

type SlotAction =
  | { type: 'SET_LOADING'; payload: boolean }
  | { type: 'SET_ERROR'; payload: string | null }
  | { type: 'SET_SLOTS'; payload: Slot[] }
  | { type: 'ADD_SLOT_SUCCESS'; payload: AddSlotResponse }
  | { type: 'CLEAR_LAST_ADDED' };

const initialState: SlotState = {
  slots: [],
  loading: false,
  error: null,
  lastAddedSlot: null,
};

const slotReducer = (state: SlotState, action: SlotAction): SlotState => {
  switch (action.type) {
    case 'SET_LOADING':
      return { ...state, loading: action.payload };
    case 'SET_ERROR':
      return { ...state, error: action.payload };
    case 'SET_SLOTS':
      return { ...state, slots: action.payload };
    case 'ADD_SLOT_SUCCESS':
      return { ...state, lastAddedSlot: action.payload };
    case 'CLEAR_LAST_ADDED':
      return { ...state, lastAddedSlot: null };
    default:
      return state;
  }
};

const SlotContext = createContext<{
  state: SlotState;
  dispatch: React.Dispatch<SlotAction>;
} | null>(null);

export const SlotProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [state, dispatch] = useReducer(slotReducer, initialState);

  return (
    <SlotContext.Provider value={{ state, dispatch }}>
      {children}
    </SlotContext.Provider>
  );
};

export const useSlotStore = () => {
  const context = useContext(SlotContext);
  if (!context) {
    throw new Error('useSlotStore must be used within an SlotProvider');
  }
  return context;
};

export const useSlot = () => {
  const { state, dispatch } = useSlotStore();

  const addSlot = async (slotData: AddSlotRequest): Promise<AddSlotResponse> => {
    try {
      dispatch({ type: 'SET_LOADING', payload: true });
      dispatch({ type: 'SET_ERROR', payload: null });
      
      const result = await slotService.addSlot(slotData);
      dispatch({ type: 'ADD_SLOT_SUCCESS', payload: result });
      return result;
    } catch (error: any) {
      const errorMessage = error.response?.data || error.message || 'Failed to add slot';
      dispatch({ type: 'SET_ERROR', payload: errorMessage });
      throw error;
    } finally {
      dispatch({ type: 'SET_LOADING', payload: false });
    }
  };

  const getSlots = async (page: number = 1, count: number = 3): Promise<void> => {
    try {
      dispatch({ type: 'SET_LOADING', payload: true });
      dispatch({ type: 'SET_ERROR', payload: null });
      
      const response = await slotService.getSlots(page, count);
      dispatch({ type: 'SET_SLOTS', payload: response.slots });
    } catch (error: any) {
      const errorMessage = error.response?.data || error.message || 'Failed to get slots';
      dispatch({ type: 'SET_ERROR', payload: errorMessage });
      throw error;
    } finally {
      dispatch({ type: 'SET_LOADING', payload: false });
    }
  };

  const clearLastAdded = (): void => {
    dispatch({ type: 'CLEAR_LAST_ADDED' });
  };

  return {
    slots: state.slots,
    loading: state.loading,
    error: state.error,
    lastAddedSlot: state.lastAddedSlot,
    addSlot,
    getSlots,
    clearLastAdded,
  };
};