import { createContext, useContext, useReducer } from 'react';
import type { Checkpoint, PassCheckpointResponse } from '../models/checkpoint';
import { checkpointService } from '../services/checkpointService';

interface CheckpointState {
  checkpoints: Checkpoint[];
  loading: boolean;
  error: string | null;
  passResult: PassCheckpointResponse | null;
}

type CheckpointAction =
  | { type: 'SET_LOADING'; payload: boolean }
  | { type: 'SET_ERROR'; payload: string | null }
  | { type: 'SET_CHECKPOINTS'; payload: Checkpoint[] }
  | { type: 'SET_PASS_RESULT'; payload: PassCheckpointResponse }
  | { type: 'CLEAR_PASS_RESULT' };

const initialState: CheckpointState = {
  checkpoints: [],
  loading: false,
  error: null,
  passResult: null,
};

const checkpointReducer = (state: CheckpointState, action: CheckpointAction): CheckpointState => {
  switch (action.type) {
    case 'SET_LOADING':
      return { ...state, loading: action.payload };
    case 'SET_ERROR':
      return { ...state, error: action.payload };
    case 'SET_CHECKPOINTS':
      return { ...state, checkpoints: action.payload };
    case 'SET_PASS_RESULT':
      return { ...state, passResult: action.payload };
    case 'CLEAR_PASS_RESULT':
      return { ...state, passResult: null };
    default:
      return state;
  }
};

const CheckpointContext = createContext<{
  state: CheckpointState;
  dispatch: React.Dispatch<CheckpointAction>;
} | null>(null);

export const CheckpointProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [state, dispatch] = useReducer(checkpointReducer, initialState);

  return (
    <CheckpointContext.Provider value={{ state, dispatch }}>
      {children}
    </CheckpointContext.Provider>
  );
};

export const useCheckpointStore = () => {
  const context = useContext(CheckpointContext);
  if (!context) {
    throw new Error('useCheckpointStore must be used within an CheckpointProvider');
  }
  return context;
};

export const useCheckpoint = () => {
  const { state, dispatch } = useCheckpointStore();

  const getCheckpoints = async (page: number = 1, count: number = 5): Promise<void> => {
    try {
      dispatch({ type: 'SET_LOADING', payload: true });
      dispatch({ type: 'SET_ERROR', payload: null });
      
      const response = await checkpointService.getCheckpoints(page, count);
      dispatch({ type: 'SET_CHECKPOINTS', payload: response.checkpoints });
    } catch (error: any) {
      const errorMessage = error.response?.data || error.message || 'Failed to get checkpoints';
      dispatch({ type: 'SET_ERROR', payload: errorMessage });
      throw error;
    } finally {
      dispatch({ type: 'SET_LOADING', payload: false });
    }
  };

  const passCheckpoint = async (checkpointId: string): Promise<PassCheckpointResponse> => {
    try {
      dispatch({ type: 'SET_LOADING', payload: true });
      dispatch({ type: 'SET_ERROR', payload: null });
      
      const result = await checkpointService.passCheckpoint(checkpointId);
      dispatch({ type: 'SET_PASS_RESULT', payload: result });
      return result;
    } catch (error: any) {
      const errorMessage = error.response?.data || error.message || 'Failed to pass checkpoint';
      dispatch({ type: 'SET_ERROR', payload: errorMessage });
      throw error;
    } finally {
      dispatch({ type: 'SET_LOADING', payload: false });
    }
  };

  const clearPassResult = (): void => {
    dispatch({ type: 'CLEAR_PASS_RESULT' });
  };

  return {
    checkpoints: state.checkpoints,
    loading: state.loading,
    error: state.error,
    passResult: state.passResult,
    getCheckpoints,
    passCheckpoint,
    clearPassResult,
  };
};