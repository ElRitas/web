import React, { useState } from 'react';
import { useAuth } from '../../stores/authStore';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';

export const RegisterForm: React.FC = () => {
  const { register, loading, error } = useAuth();
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    address: '',
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await register(formData);
      // Редирект или обновление интерфейса произойдет автоматически через store
    } catch (error) {
      // Ошибка уже обработана в store
    }
  };

  const handleChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  return (
    <form onSubmit={handleSubmit} className="max-w-md mx-auto p-6 bg-white rounded-lg shadow-md">
      <h2 className="text-2xl font-bold mb-6 text-center">Регистрация</h2>
      
      {error && (
        <div className="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded">
          {error}
        </div>
      )}

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

      <Button
        type="submit"
        variant="primary"
        loading={loading}
        disabled={loading}
        className="w-full"
      >
        Зарегистрироваться
      </Button>
    </form>
  );
};