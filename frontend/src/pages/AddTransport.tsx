import { type JSX, useState } from "react";
import { useAuth } from "../stores/authStore";
import { useTransport } from "../stores/transportStore";
import { useNavigate } from "react-router-dom";
import { Header } from "../components/layout/Header";
import { Input } from "../components/ui/Input";
import "./AddTransport.css";

export const AddTransport = (): JSX.Element => {
  const { user, isAuthenticated } = useAuth();
  const { addTransport, loading, error } = useTransport();
  const navigate = useNavigate();
  
  const [formData, setFormData] = useState({
    number: "",
    model: "",
    color: "",
    insurance: false
  });

  const [validationErrors, setValidationErrors] = useState({
    number: "",
    model: "",
    color: ""
  });

  const handleInputChange = (field: string, value: string | boolean) => {
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
      number: "",
      model: "",
      color: ""
    };

    let isValid = true;

    if (!formData.number.trim()) {
      errors.number = "Номер транспортного средства обязателен";
      isValid = false;
    }

    if (!formData.model.trim()) {
      errors.model = "Модель обязательна для заполнения";
      isValid = false;
    }

    if (!formData.color.trim()) {
      errors.color = "Цвет обязателен для заполнения";
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
      await addTransport(formData);
      navigate("/dashboard");
    } catch (err) {
      console.error("Failed to add transport:", err);
    }
  };

  if (!isAuthenticated || !user) {
    navigate("/");
    return <></>;
  }

  return (
    <div className="add-transport">
      <Header />
      <div className="add-transport-content">
        <form className="auth-form" onSubmit={handleSubmit}>
          
          {error && (
            <div className="error-message">
              {error}
            </div>
          )}

          <div className="form-fields">
            <Input
              label="Номер транспортного средства"
              type="text"
              value={formData.number}
              onChange={(value) => handleInputChange('number', value)}
              placeholder="Введите номер"
              required
              error={validationErrors.number}
            />

            <Input
              label="Модель"
              type="text"
              value={formData.model}
              onChange={(value) => handleInputChange('model', value)}
              placeholder="Введите модель"
              required
              error={validationErrors.model}
            />

            <Input
              label="Цвет"
              type="text"
              value={formData.color}
              onChange={(value) => handleInputChange('color', value)}
              placeholder="Введите цвет"
              required
              error={validationErrors.color}
            />

            <div className="checkbox-group">
              <label className="checkbox-label">
                <input
                  type="checkbox"
                  checked={formData.insurance}
                  onChange={(e) => handleInputChange('insurance', e.target.checked)}
                  className="checkbox-input"
                />
                <span className="checkbox-text">Страховка оформлена</span>
              </label>
            </div>
          </div>

          <button 
            type="submit" 
            className="submit-button"
            disabled={loading}
          >
            {loading ? "Добавление..." : "Добавить транспорт"}
          </button>
        </form>
      </div>
    </div>
  );
};