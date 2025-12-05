import { type JSX, useState, useEffect } from "react";
import { useAuth } from "../stores/authStore";
import { useSlot } from "../stores/slotStore";
import { useNavigate } from "react-router-dom";
import { Header } from "../components/layout/Header";
import { Input } from "../components/ui/Input";
import "./AddSlot.css";

export const AddSlot = (): JSX.Element => {
  const { user, isAuthenticated } = useAuth();
  const { addSlot, loading, error, lastAddedSlot, clearLastAdded } = useSlot();
  const navigate = useNavigate();
  
  const [formData, setFormData] = useState({
    slotNumber: ""
  });

  const [validationErrors, setValidationErrors] = useState({
    slotNumber: ""
  });

  useEffect(() => {
    if (lastAddedSlot) {
      // Автоматически скрываем сообщение через 3 секунды и очищаем форму
      const timer = setTimeout(() => {
        clearLastAdded();
        setFormData({ slotNumber: "" });
      }, 3000);
      
      return () => clearTimeout(timer);
    }
  }, [lastAddedSlot]);

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
    
    // Очищаем ошибку валидации при вводе
    if (validationErrors[field as keyof typeof validationErrors]) {
      setValidationErrors(prev => ({
        ...prev,
        [field]: ""
      }));
    }
  };

  const validateForm = (): boolean => {
    const errors = {
      slotNumber: ""
    };

    let isValid = true;

    if (!formData.slotNumber.trim()) {
      errors.slotNumber = "Номер места обязателен для заполнения";
      isValid = false;
    } else if (!/^\d+$/.test(formData.slotNumber)) {
      errors.slotNumber = "Номер места должен быть числом";
      isValid = false;
    } else if (parseInt(formData.slotNumber) <= 0) {
      errors.slotNumber = "Номер места должен быть положительным числом";
      isValid = false;
    }

    setValidationErrors(errors);
    return isValid;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }
    
    try {
      await addSlot({
        residentialId: user!.residentialId,
        slotNumber: parseInt(formData.slotNumber)
      });
    } catch (err) {
      console.error("Failed to add slot:", err);
    }
  };

  if (!isAuthenticated || !user) {
    navigate("/");
    return <></>;
  }

  return (
    <div className="add-slot">
      <Header />
      <div className="add-slot-content">
        <form className="auth-form" onSubmit={handleSubmit}>
          
          <h1 className="form-title">Добавить парковочное место</h1>

          {error && (
            <div className="error-message">
              {error}
            </div>
          )}

          {lastAddedSlot && (
            <div className="success-message">
              Парковочное место успешно добавлено! ID: {lastAddedSlot.slotId}
            </div>
          )}

          <div className="form-fields">
            <Input
              label="Номер парковочного места"
              type="text"
              value={formData.slotNumber}
              onChange={(value) => handleInputChange('slotNumber', value)}
              placeholder="Введите номер места"
              required
              error={validationErrors.slotNumber}
            />
          </div>

          <button 
            type="submit" 
            className="submit-button"
            disabled={loading}
          >
            {loading ? "Добавление..." : "Добавить место"}
          </button>
        </form>
      </div>
    </div>
  );
};