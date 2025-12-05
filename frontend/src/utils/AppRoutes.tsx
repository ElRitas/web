import { Routes, Route } from 'react-router-dom';
import { HomePage } from '../pages/HomePage';
import { LoginPage } from '../pages/LoginPage';
import { RegisterPage } from '../pages/RegisterPage';
import { UserDashboard } from '../pages/UserDashboard';
import { AddGuest } from '../pages/AddGuest';
import { AddTransport } from '../pages/AddTransport';
import { CheckpointPage } from '../pages/CheckpointPage';
import { AddSlot } from '../pages/AddSlot';
import { AdminReservations } from '../pages/AdminReservations';
import { ResidentReservations } from '../pages/ResidentReservations';

export const AppRoutes = () => {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/dashboard" element={<UserDashboard />} />
      <Route path="/add-guest" element={<AddGuest />} />
      <Route path="/add-transport" element={<AddTransport />} />
      <Route path="/checkpoint" element={<CheckpointPage />} />
      <Route path="/add-slot" element={<AddSlot />} />
      <Route path="/admin-reservations" element={<AdminReservations />} />
      <Route path="/resident-reservations" element={<ResidentReservations />} />
    </Routes>
  );
};