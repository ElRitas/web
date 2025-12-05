import { type JSX, useState } from "react";
import { useAuth } from "../stores/authStore";
import { useNavigate, Link } from "react-router-dom";
import { Header } from "../components/layout/Header";
import { EnterButton } from "../components/ui/EnterButton";
import { Input } from "../components/ui/Input";
import "./LoginPage.css";

export const LoginPage = (): JSX.Element => {
  const { login, error } = useAuth();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    email: "",
    password: "",
  });

  const handleSubmit = async (e: React.FormEvent) => {
  e.preventDefault();
  try {
    await login(formData);
    navigate("/dashboard");
  } catch (error) {
  }
};

  const handleChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  return (
    <div className="login-page">
      <Header />
      <div className="login-content">
        <form className="auth-form" onSubmit={handleSubmit}>
          
          {error && (
            <div className="error-message">
              {error}
            </div>
          )}

          <div className="form-fields">
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
              placeholder="Введите ваш пароль"
              required
            />
          </div>

          <EnterButton
            variant="login"
            onClick={() => {}} // Обработчик в форме
            className="submit-button"
          />

          <div className="switch-auth">
            Нет аккаунта? <Link to="/register">Зарегистрироваться</Link>
          </div>
        </form>
      </div>
    </div>
  );
};