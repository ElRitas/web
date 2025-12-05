import { type JSX, useState } from "react";
import { useAuth } from "../stores/authStore";
import { useNavigate, Link } from "react-router-dom";
import { Header } from "../components/layout/Header";
import { EnterButton } from "../components/ui/EnterButton";
import { Input } from "../components/ui/Input";
import "./RegisterPage.css";

export const RegisterPage = (): JSX.Element => {
  const { register, error } = useAuth();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    name: "",
    email: "",
    password: "",
    address: "",
  });

  const handleSubmit = async (e: React.FormEvent) => {
  e.preventDefault();
  try {
    await register(formData);
    navigate("/dashboard");
  } catch (error) {
  }
};

  const handleChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  return (
    <div className="register-page">
      <Header />
      <div className="register-content">
        <form className="auth-form" onSubmit={handleSubmit}>
          
          {error && (
            <div className="error-message">
              {error}
            </div>
          )}

          <div className="form-fields">
            <Input
              label="ФИО"
              value={formData.name}
              onChange={(value) => handleChange('name', value)}
              placeholder="Введите ваше ФИО"
              required
            />

            <Input
              label="Email"
              type="email"
              value={formData.email}
              onChange={(value) => handleChange('email', value)}
              placeholder="Введите ваш email"
              required
            />

            <Input
              label="Пароль"
              type="password"
              value={formData.password}
              onChange={(value) => handleChange('password', value)}
              placeholder="Придумайте пароль"
              required
            />

            <Input
              label="Адрес"
              value={formData.address}
              onChange={(value) => handleChange('address', value)}
              placeholder="Введите ваш адрес"
              required
            />
          </div>

          <EnterButton
            variant="register"
            onClick={() => {}} // Обработчик в форме
            className="submit-button"
          />

          <div className="switch-auth">
            Уже есть аккаунт? <Link to="/login">Войти</Link>
          </div>
        </form>
      </div>
    </div>
  );
};