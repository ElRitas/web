// import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import { AuthProvider } from './stores/authStore';
import { Layout } from './components/layout/Layout';
import './App.css';
import { TransportProvider } from './stores/transportStore';
import { CheckpointProvider } from './stores/checkpointStore';
import { SlotProvider } from './stores/slotStore';
import { ReservationProvider } from './stores/reservationStore';
import { AppRoutes } from './utils/AppRoutes';

function App() {
  return (
    <AuthProvider>
      <TransportProvider>
        <CheckpointProvider>
          <SlotProvider>
            <ReservationProvider>
              <Router>
                <Layout>
                  <AppRoutes />
                </Layout>
              </Router>
            </ReservationProvider>
          </SlotProvider>
        </CheckpointProvider>
      </TransportProvider>
    </AuthProvider>
  );
}

export default App;