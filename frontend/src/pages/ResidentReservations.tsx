import { type JSX, useEffect, useState } from "react";
import { useAuth } from "../stores/authStore";
import { useReservation } from "../stores/reservationStore";
import { useTransport } from "../stores/transportStore";
import { useSlot } from "../stores/slotStore";
import { useNavigate } from "react-router-dom";
import { Header } from "../components/layout/Header";
import "./Reservations.css";

export const ResidentReservations = (): JSX.Element => {
  const { user, isAuthenticated } = useAuth();
  const { addReservation, loading, error, successMessage, clearMessages } = useReservation();
  const { transports, getTransports } = useTransport();
  const { slots, getSlots } = useSlot();
  const navigate = useNavigate();
  
  const [selectedSlot, setSelectedSlot] = useState<string | null>(null);
  const [selectedTransport, setSelectedTransport] = useState<string>("");

  useEffect(() => {
    if (isAuthenticated && user && user.role === 'RESIDENT') {
      getSlots();
      getTransports();
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

  const handleBookSlot = async (slotId: string) => {
    if (!selectedTransport) {
      alert('Пожалуйста, выберите транспорт');
      return;
    }

    try {
      setSelectedSlot(slotId);
      await addReservation({
        slotId: slotId,
        transportId: selectedTransport
      });
      setSelectedSlot(null);
      getSlots();
    } catch (err) {
      console.error("Failed to book slot:", err);
      setSelectedSlot(null);
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'FREE': return 'Свободно';
      case 'OCCUPIED_BY_ME': return 'Занято вами';
      case 'OCCUPIED_BY_OTHER': return 'Занято';
      default: return status;
    }
  };

  const getStatusClass = (status: string) => {
    switch (status) {
      case 'FREE': return 'free';
      case 'OCCUPIED_BY_ME': return 'occupied-me';
      case 'OCCUPIED_BY_OTHER': return 'occupied-other';
      default: return status;
    }
  };

  if (!isAuthenticated || !user || user.role !== 'RESIDENT') {
    navigate("/");
    return <></>;
  }

  return (
    <div className="reservations-page">
      <Header />
      <div className="reservations-content">
        <div className="reservations-container">
          <h1 className="reservations-title">Бронирование парковочных мест</h1>
          
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

          {/* Выбор транспорта */}
          <div className="transport-selection">
            <div className="selection-group">
              <label htmlFor="transport-select" className="selection-label">
                Выберите транспорт для бронирования:
              </label>
              <select
                id="transport-select"
                value={selectedTransport}
                onChange={(e) => setSelectedTransport(e.target.value)}
                className="transport-select"
                disabled={loading}
              >
                <option value="">-- Выберите транспорт --</option>
                {transports.map(transport => (
                  <option key={transport.id} value={transport.id}>
                    {transport.model} ({transport.number}) {transport.color}
                  </option>
                ))}
              </select>
            </div>
            
            {transports.length === 0 && !loading && (
              <div className="no-transports">
                У вас нет зарегистрированного транспорта.
              </div>
            )}
          </div>
          <div className="slots-section">
            
            {loading && slots.length === 0 ? (
              <div className="loading">Загрузка мест...</div>
            ) : (
              <div className="slots-grid">
                {slots.map((slot) => {
                  const slotStatus = slot.status;
                  return (
                    <div key={slot.id} className={`slot-card ${getStatusClass(slotStatus)}`}>
                      <div className="slot-header">
                        <h3 className="slot-number">Место #{slot.number}</h3>
                        <span className={`status ${getStatusClass(slotStatus)}`}>
                          {getStatusText(slotStatus)}
                        </span>
                      </div>
                      
                      <div className="slot-info">
                        <div className="info-item">
                          <strong>ID места:</strong> {slot.id}
                        </div>
                        <div className="info-item">
                          <strong>Статус:</strong> {slot.status || 'Неизвестно'}
                        </div>
                      </div>

                      {slotStatus === 'FREE' && (
                        <button
                          className="book-button"
                          onClick={() => handleBookSlot(slot.id)}
                          disabled={loading || !selectedTransport || selectedSlot === slot.id}
                        >
                          {loading && selectedSlot === slot.id ? "Бронирование..." : "Забронировать"}
                        </button>
                      )}

                      {slotStatus === slotStatus.at(1) && (
                        <div className="my-booking">
                          Занято
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            )}

            {!loading && slots.length === 0 && (
              <div className="no-slots">
                Нет доступных парковочных мест
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};