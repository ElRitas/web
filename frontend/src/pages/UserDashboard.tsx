import { type JSX, useEffect } from "react";
import { useAuth } from "../stores/authStore";
import { useNavigate } from "react-router-dom";
import { Header } from "../components/layout/Header";
import { DashboardButton } from "../components/ui/DashboardButton";
import "./UserDashboard.css";

export const UserDashboard = (): JSX.Element => {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    document.title = "Личный кабинет - Parking App";
  }, []);

  useEffect(() => {
    if (!isAuthenticated || !user) {
      navigate("/");
    }
  }, [isAuthenticated, user, navigate]);

  const handleAddCar = () => {
    navigate("/add-transport");
  };

  const handleCheckpoint = () => {
    navigate("/checkpoint");
  };

  const handleManageBookings = () => {
  if (user!.role === 'ADMIN') {
    navigate("/admin-reservations");
  } else {
    navigate("/resident-reservations");
  }
};

  const handleAddGuest = () => {
    navigate("/add-guest");
  };

  const handleAddSlots = () => {
    navigate("/add-slot");
  };

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  const renderResidentButtons = () => (
    <>
      <DashboardButton
        title="Добавить авто"
        onClick={handleAddCar}
        className="action-button"
      />
      <DashboardButton
        title="Проехать через КПП"
        onClick={handleCheckpoint}
        className="action-button"
      />
      <DashboardButton
        title="Управление бронями"
        onClick={handleManageBookings}
        className="action-button"
      />
      <DashboardButton
        title="Добавить гостя"
        onClick={handleAddGuest}
        className="action-button"
      />
    </>
  );

  const renderGuestButtons = () => (
    <DashboardButton
      title="Проехать через КПП"
      onClick={handleCheckpoint}
      className="action-button"
    />
  );

  const renderAdminButtons = () => (
    <>
      <DashboardButton
        title="Добавить места"
        onClick={handleAddSlots}
        className="action-button"
      />
      <DashboardButton
        title="Управление бронями"
        onClick={handleManageBookings}
        className="action-button"
      />
    </>
  );

  const renderActionButtons = () => {
    switch (user!.role) {
      case "RESIDENT":
        return renderResidentButtons();
      case "GUEST":
        return renderGuestButtons();
      case "ADMIN":
        return renderAdminButtons();
      default:
        return null;
    }
  };

  return (
    <div className="user-dashboard">
      <Header />
      <div className="dashboard-content">
        <div className="welcome-section">
          <h1 className="welcome-title">
            Привет, {user!.fio}!
          </h1>
        </div>

        {/* Кнопки действий */}
        <div className="actions-section">
          <div className="actions-grid">
            {renderActionButtons()}
          </div>
        </div>

        {/* Кнопка выхода */}
        <div className="logout-section">
          <button 
            className="logout-btn"
            onClick={handleLogout}
          >
            Выйти из системы
          </button>
        </div>
      </div>
    </div>
  );
};