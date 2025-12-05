import { createContext, useContext, useReducer } from 'react';
import type { 
  ReservationInfo, 
  AddReservationRequest, 
  UpdateReservationStatusRequest,
  SlotWithStatus 
} from '../models/reservation';
import { reservationService } from '../services/reservationService';

interface ReservationState {
  reservations: ReservationInfo[];
  slots: SlotWithStatus[];
  loading: boolean;
  error: string | null;
  successMessage: string | null;
}

type ReservationAction =
  | { type: 'SET_LOADING'; payload: boolean }
  | { type: 'SET_ERROR'; payload: string | null }
  | { type: 'SET_SUCCESS_MESSAGE'; payload: string | null }
  | { type: 'SET_RESERVATIONS'; payload: ReservationInfo[] }
  | { type: 'SET_SLOTS'; payload: SlotWithStatus[] }
  | { type: 'ADD_RESERVATION'; payload: ReservationInfo }
  | { type: 'DELETE_RESERVATION'; payload: string }
  | { type: 'UPDATE_RESERVATION_STATUS'; payload: { id: string; status: string } };

const initialState: ReservationState = {
  reservations: [],
  slots: [],
  loading: false,
  error: null,
  successMessage: null,
};

const reservationReducer = (state: ReservationState, action: ReservationAction): ReservationState => {
  switch (action.type) {
    case 'SET_LOADING':
      return { ...state, loading: action.payload };
    case 'SET_ERROR':
      return { ...state, error: action.payload, successMessage: null };
    case 'SET_SUCCESS_MESSAGE':
      return { ...state, successMessage: action.payload, error: null };
    case 'SET_RESERVATIONS':
      return { ...state, reservations: action.payload };
    case 'SET_SLOTS':
      return { ...state, slots: action.payload };
    case 'ADD_RESERVATION':
      return { ...state, reservations: [...state.reservations, action.payload] };
    case 'DELETE_RESERVATION':
      return { 
        ...state, 
        reservations: state.reservations.filter(reservation => reservation.id !== action.payload) 
      };
    case 'UPDATE_RESERVATION_STATUS':
      return {
        ...state,
        reservations: state.reservations.map(reservation =>
          reservation.id === action.payload.id 
            ? { ...reservation, status: action.payload.status as any }
            : reservation
        )
      };
    default:
      return state;
  }
};

const ReservationContext = createContext<{
  state: ReservationState;
  dispatch: React.Dispatch<ReservationAction>;
} | null>(null);

export const ReservationProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [state, dispatch] = useReducer(reservationReducer, initialState);

  return (
    <ReservationContext.Provider value={{ state, dispatch }}>
      {children}
    </ReservationContext.Provider>
  );
};

export const useReservationStore = () => {
  const context = useContext(ReservationContext);
  if (!context) {
    throw new Error('useReservationStore must be used within an ReservationProvider');
  }
  return context;
};

export const useReservation = () => {
  const { state, dispatch } = useReservationStore();

  const addReservation = async (reservationData: AddReservationRequest): Promise<void> => {
    try {
      dispatch({ type: 'SET_LOADING', payload: true });
      dispatch({ type: 'SET_ERROR', payload: null });
      
      await reservationService.addReservation(reservationData);
      dispatch({ type: 'SET_SUCCESS_MESSAGE', payload: 'Бронь успешно создана' });
      
      // Обновляем список броней после добавления
      await getReservations();
    } catch (error: any) {
      const errorMessage = error.response?.data || error.message || 'Failed to add reservation';
      dispatch({ type: 'SET_ERROR', payload: errorMessage });
      throw error;
    } finally {
      dispatch({ type: 'SET_LOADING', payload: false });
    }
  };

  const getReservations = async (page: number = 1, count: number = 10): Promise<void> => {
    try {
      dispatch({ type: 'SET_LOADING', payload: true });
      dispatch({ type: 'SET_ERROR', payload: null });
      
      const response = await reservationService.getReservations(page, count);
      dispatch({ type: 'SET_RESERVATIONS', payload: response.reservations });
    } catch (error: any) {
      const errorMessage = error.response?.data || error.message || 'Failed to get reservations';
      dispatch({ type: 'SET_ERROR', payload: errorMessage });
      throw error;
    } finally {
      dispatch({ type: 'SET_LOADING', payload: false });
    }
  };

  const deleteReservation = async (reservationId: string): Promise<void> => {
    try {
      dispatch({ type: 'SET_LOADING', payload: true });
      dispatch({ type: 'SET_ERROR', payload: null });
      
      await reservationService.deleteReservation(reservationId);
      dispatch({ type: 'DELETE_RESERVATION', payload: reservationId });
      dispatch({ type: 'SET_SUCCESS_MESSAGE', payload: 'Бронь успешно отменена' });
    } catch (error: any) {
      const errorMessage = error.response?.data || error.message || 'Failed to delete reservation';
      dispatch({ type: 'SET_ERROR', payload: errorMessage });
      throw error;
    } finally {
      dispatch({ type: 'SET_LOADING', payload: false });
    }
  };

  const updateReservationStatus = async (reservationId: string, statusData: UpdateReservationStatusRequest): Promise<void> => {
    try {
      dispatch({ type: 'SET_LOADING', payload: true });
      dispatch({ type: 'SET_ERROR', payload: null });
      
      await reservationService.updateReservationStatus(reservationId, statusData);
      dispatch({ type: 'UPDATE_RESERVATION_STATUS', payload: { id: reservationId, status: statusData.status } });
      dispatch({ type: 'SET_SUCCESS_MESSAGE', payload: 'Статус брони обновлен' });
    } catch (error: any) {
      const errorMessage = error.response?.data || error.message || 'Failed to update reservation status';
      dispatch({ type: 'SET_ERROR', payload: errorMessage });
      throw error;
    } finally {
      dispatch({ type: 'SET_LOADING', payload: false });
    }
  };

  const clearMessages = (): void => {
    dispatch({ type: 'SET_ERROR', payload: null });
    dispatch({ type: 'SET_SUCCESS_MESSAGE', payload: null });
  };

  return {
    reservations: state.reservations,
    slots: state.slots,
    loading: state.loading,
    error: state.error,
    successMessage: state.successMessage,
    addReservation,
    getReservations,
    deleteReservation,
    updateReservationStatus,
    clearMessages,
  };
};