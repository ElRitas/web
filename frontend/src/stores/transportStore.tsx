import React, { createContext, useContext, useReducer } from 'react';
import type { Transport, AddTransportRequest } from '../models/transport';
import { transportService } from '../services/transportService';

interface TransportState {
  transports: Transport[];
  loading: boolean;
  error: string | null;
}

type TransportAction =
  | { type: 'SET_LOADING'; payload: boolean }
  | { type: 'SET_ERROR'; payload: string | null }
  | { type: 'SET_TRANSPORTS'; payload: Transport[] }
  | { type: 'ADD_TRANSPORT'; payload: Transport }
  | { type: 'DELETE_TRANSPORT'; payload: string };

const initialState: TransportState = {
  transports: [],
  loading: false,
  error: null,
};

const transportReducer = (state: TransportState, action: TransportAction): TransportState => {
  switch (action.type) {
    case 'SET_LOADING':
      return { ...state, loading: action.payload };
    case 'SET_ERROR':
      return { ...state, error: action.payload };
    case 'SET_TRANSPORTS':
      return { ...state, transports: action.payload };
    case 'ADD_TRANSPORT':
      return { ...state, transports: [...state.transports, action.payload] };
    case 'DELETE_TRANSPORT':
      return { 
        ...state, 
        transports: state.transports.filter(transport => transport.id !== action.payload) 
      };
    default:
      return state;
  }
};

const TransportContext = createContext<{
  state: TransportState;
  dispatch: React.Dispatch<TransportAction>;
} | null>(null);

export const TransportProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [state, dispatch] = useReducer(transportReducer, initialState);

  return (
    <TransportContext.Provider value={{ state, dispatch }}>
      {children}
    </TransportContext.Provider>
  );
};

export const useTransportStore = () => {
  const context = useContext(TransportContext);
  if (!context) {
    throw new Error('useTransportStore must be used within an TransportProvider');
  }
  return context;
};

export const useTransport = () => {
  const { state, dispatch } = useTransportStore();

  const addTransport = async (transportData: AddTransportRequest): Promise<void> => {
    try {
      dispatch({ type: 'SET_LOADING', payload: true });
      dispatch({ type: 'SET_ERROR', payload: null });
      
      await transportService.addTransport(transportData);
    
      dispatch({ type: 'SET_LOADING', payload: false });
    } catch (error: any) {
      const errorMessage = error.response?.data || error.message || 'Failed to add transport';
      dispatch({ type: 'SET_ERROR', payload: errorMessage });
      throw error;
    } finally {
      dispatch({ type: 'SET_LOADING', payload: false });
    }
  };

  const deleteTransport = async (transportId: string): Promise<void> => {
    try {
      dispatch({ type: 'SET_LOADING', payload: true });
      dispatch({ type: 'SET_ERROR', payload: null });
      
      await transportService.deleteTransport(transportId);
      dispatch({ type: 'DELETE_TRANSPORT', payload: transportId });
    } catch (error: any) {
      const errorMessage = error.response?.data || error.message || 'Failed to delete transport';
      dispatch({ type: 'SET_ERROR', payload: errorMessage });
      throw error;
    } finally {
      dispatch({ type: 'SET_LOADING', payload: false });
    }
  };

  const getTransports = async (page: number = 1, count: number = 10): Promise<void> => {
    try {
      dispatch({ type: 'SET_LOADING', payload: true });
      dispatch({ type: 'SET_ERROR', payload: null });
      
      const response = await transportService.getTransports(page, count);
      dispatch({ type: 'SET_TRANSPORTS', payload: response.transport });
    } catch (error: any) {
      const errorMessage = error.response?.data || error.message || 'Failed to get slots';
      dispatch({ type: 'SET_ERROR', payload: errorMessage });
      throw error;
    } finally {
      dispatch({ type: 'SET_LOADING', payload: false });
    }
  };

  return {
    transports: state.transports,
    loading: state.loading,
    error: state.error,
    addTransport,
    deleteTransport,
    getTransports
  };
};