import { type JSX, useEffect, useState } from "react";
import { useAuth } from "../stores/authStore";
import { useCheckpoint } from "../stores/checkpointStore";
import { useNavigate } from "react-router-dom";
import { Header } from "../components/layout/Header";
import "./CheckpointPage.css";

export const CheckpointPage = (): JSX.Element => {
  const { user, isAuthenticated } = useAuth();
  const { checkpoints, loading, error, passResult, getCheckpoints, passCheckpoint, clearPassResult } = useCheckpoint();
  const navigate = useNavigate();
  const [selectedCheckpoint, setSelectedCheckpoint] = useState<string | null>(null);

  useEffect(() => {
    if (isAuthenticated && user) {
      getCheckpoints();
    }
  }, [isAuthenticated, user]);

  useEffect(() => {
    if (passResult) {
      const timer = setTimeout(() => {
        clearPassResult();
      }, 3000);
      
      return () => clearTimeout(timer);
    }
  }, [passResult]);

  const handlePassCheckpoint = async (checkpointId: string) => {
    setSelectedCheckpoint(checkpointId);
    try {
      await passCheckpoint(checkpointId);
    } catch (err) {
      console.error("Failed to pass checkpoint:", err);
    } finally {
      setSelectedCheckpoint(null);
    }
  };

  if (!isAuthenticated || !user) {
    navigate("/");
    return <></>;
  }

  return (
    <div className="checkpoint-page">
      <Header />
      <div className="checkpoint-content">
        <div className="checkpoint-container">
          <h1 className="checkpoint-title">Доступные КПП</h1>
          
          {error && (
            <div className="error-message">
              {error}
            </div>
          )}

          {passResult && (
            <div className={`pass-result ${passResult.passed ? 'success' : 'error'}`}>
              {passResult.passed 
                ? "Успешно! Вы прошли через КПП" 
                : "Не удалось пройти через КПП"
              }
            </div>
          )}

          {loading && !checkpoints.length ? (
            <div className="loading">Загрузка КПП...</div>
          ) : (
            <div className="checkpoints-grid">
              {checkpoints.map((checkpoint) => (
                <div key={checkpoint.id} className="checkpoint-card">
                  <div className="checkpoint-header">
                    <h3 className="checkpoint-number">КПП #{checkpoint.number}</h3>
                    <span className={`status ${checkpoint.status.toLowerCase()}`}>
                      {checkpoint.status === 'OK' ? 'Работает' : 'Не работает'}
                    </span>
                  </div>
                  
                  <div className="checkpoint-info">
                    <div className="info-item">
                      <strong>Охраник:</strong> {checkpoint.guardFio}
                    </div>
                    <div className="info-item">
                      <strong>Телефон:</strong> {checkpoint.guardPhone}
                    </div>
                  </div>

                  <button
                    className={`pass-button ${checkpoint.status !== 'OK' ? 'disabled' : ''}`}
                    onClick={() => handlePassCheckpoint(checkpoint.id)}
                    disabled={checkpoint.status !== 'OK' || loading || selectedCheckpoint === checkpoint.id}
                  >
                    {selectedCheckpoint === checkpoint.id ? "Проверка..." : "Проехать через КПП"}
                  </button>
                </div>
              ))}
            </div>
          )}

          {!loading && checkpoints.length === 0 && (
            <div className="no-checkpoints">
              Нет доступных контрольных пунктов
            </div>
          )}
        </div>
      </div>
    </div>
  );
};