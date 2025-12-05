import { type JSX, useEffect, useState } from "react";
import { useAuth } from "../stores/authStore";
import { useReservation } from "../stores/reservationStore";
import { useNavigate } from "react-router-dom";
import { Header } from "../components/layout/Header";
import "./Reservations.css";

export const AdminReservations = (): JSX.Element => {
  const { user, isAuthenticated } = useAuth();
  const { reservations, loading, error, successMessage, getReservations, updateReservationStatus, clearMessages } = useReservation();
  const navigate = useNavigate();
  const [selectedStatus, setSelectedStatus] = useState<{ [key: string]: string }>({});

  useEffect(() => {
    if (isAuthenticated && user && user.role === 'ADMIN') {
      getReservations();
    }
  }, [isAuthenticated, user]);

  useEffect(() => {
    if (successMessage || error) {
      const timer = setTimeout(() => {
        clearMessages();
      }, 3000);
      return () => clearTimeout(timer);
    }
  }, [successMessage, error]);

  const handleStatusChange = async (reservationId: string, newStatus: string) => {
    try {
      await updateReservationStatus(reservationId, { status: newStatus as any });
      setSelectedStatus(prev => ({ ...prev, [reservationId]: '' }));
    } catch (err) {
      console.error("Failed to update status:", err);
    }
  };

  if (!isAuthenticated || !user || user.role !== 'ADMIN') {
    navigate("/");
    return <></>;
  }

  return (
    <div className="reservations-page">
      <Header />
      <div className="reservations-content">
        <div className="reservations-container">
          <h1 className="reservations-title">Управление бронями</h1>
          
          {error && (
            <div className="error-message">
              {error}
            </div>
          )}

          {successMessage && (
            <div className="success-message">
              {successMessage}
            </div>
          )}

          {loading && !reservations.length ? (
            <div className="loading">Загрузка броней...</div>
          ) : (
            <div className="reservations-grid">
              {reservations.map((reservation) => (
                <div key={reservation.id} className="reservation-card">
                  <div className="reservation-header">
                    <h3 className="reservation-number">Место #{reservation.number}</h3>
                    <span className={`status ${reservation.status?.toLowerCase() || 'active'}`}>
                      {reservation.status === 'ACTIVE' ? 'Активна' : 
                       reservation.status === 'CANCELLED' ? 'Отменена' : 'Завершена'}
                    </span>
                  </div>
                  
                  <div className="reservation-info">
                    <div className="info-item">
                      <strong>Транспорт:</strong> {reservation.transportModel} ({reservation.transportNumber})
                    </div>
                    <div className="info-item">
                      <strong>ID брони:</strong> {reservation.id}
                    </div>
                  </div>

                  <div className="status-controls">
                    <select
                      value={selectedStatus[reservation.id] || ''}
                      onChange={(e) => setSelectedStatus(prev => ({ ...prev, [reservation.id]: e.target.value }))}
                      className="status-select"
                    >
                      <option value="">Изменить статус</option>
                      <option value="ACTIVE">Активна</option>
                      <option value="CANCELLED">Отменить</option>
                      <option value="COMPLETED">Завершить</option>
                    </select>
                    
                    {selectedStatus[reservation.id] && (
                      <button
                        className="apply-status-button"
                        onClick={() => handleStatusChange(reservation.id, selectedStatus[reservation.id])}
                        disabled={loading}
                      >
                        Применить
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}

          {!loading && reservations.length === 0 && (
            <div className="no-reservations">
              Нет активных броней
            </div>
          )}
        </div>
      </div>
    </div>
  );
};