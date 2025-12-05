import { type JSX, useState } from "react";
import { useAuth } from "../stores/authStore";
import { useNavigate } from "react-router-dom";
import { Header } from "../components/layout/Header";
import { Input } from "../components/ui/Input";
import { AddGuestButton } from "../components/ui/AddGuestButton";
import "./AddGuest.css";

export const AddGuest = (): JSX.Element => {
  const { user, isAuthenticated, addGuest, loading, error } = useAuth();
  const navigate = useNavigate();
  
  const [formData, setFormData] = useState({
    fio: "",
    email: "",
    password: ""
  });

  const [validationErrors, setValidationErrors] = useState({
    fio: "",
    email: "",
    password: ""
  });

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
      fio: "",
      email: "",
      password: ""
    };

    let isValid = true;

    if (!formData.fio.trim()) {
      errors.fio = "ФИО обязательно для заполнения";
      isValid = false;
    }

    if (!formData.email.trim()) {
      errors.email = "Email обязателен для заполнения";
      isValid = false;
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      errors.email = "Введите корректный email";
      isValid = false;
    }

    if (!formData.password) {
      errors.password = "Пароль обязателен для заполнения";
      isValid = false;
    } else if (formData.password.length < 6) {
      errors.password = "Пароль должен содержать минимум 6 символов";
      isValid = false;
    }

    setValidationErrors(errors);
    return isValid;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Валидация формы
    if (!validateForm()) {
      return;
    }
    
    try {
      await addGuest(formData);
      navigate("/dashboard");
    } catch (err) {
      console.error("Failed to add guest:", err);
    }
  };

  if (!isAuthenticated || !user) {
    navigate("/");
    return <></>;
  }

  return (
    <div className="add-guest">
      <Header />
      <div className="add-guest-content">
        <form className="auth-form" onSubmit={handleSubmit}>
          
          {/* Ошибки от API */}
          {error && (
            <div className="error-message">
              {error}
            </div>
          )}

          <div className="form-fields">
            <Input
              label="ФИО гостя"
              type="text"
              value={formData.fio}
              onChange={(value) => handleInputChange('fio', value)}
              placeholder="Введите ФИО гостя"
              required
              error={validationErrors.fio}
            />

            <Input
              label="Email"
              type="email"
              value={formData.email}
              onChange={(value) => handleInputChange('email', value)}
              placeholder="Введите email гостя"
              required
              error={validationErrors.email}
            />

            <Input
              label="Пароль"
              type="password"
              value={formData.password}
              onChange={(value) => handleInputChange('password', value)}
              placeholder="Введите пароль"
              required
              error={validationErrors.password}
            />
          </div>

          <AddGuestButton
            loading={loading}
            onClick={() => {}}
            className="submit-button"
          />
        </form>
      </div>
    </div>
  );
};