import React, { useState } from 'react';
import { useAuth } from '../../stores/authStore';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';

export const AddGuestForm: React.FC = () => {
  const { addGuest, loading, error, user } = useAuth();
  const [formData, setFormData] = useState({
    fio: '',
    email: '',
    password: '',
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await addGuest(formData);
      setFormData({ fio: '', email: '', password: '' });
      alert('Гость успешно добавлен!');
    } catch (error) {
    }
  };

  const handleChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const canAddGuest = user && user.role !== 'GUEST';

  if (!canAddGuest) {
    return (
      <div className="max-w-md mx-auto p-6 bg-yellow-100 rounded-lg shadow-md">
        <p className="text-yellow-800">
          У вас нет прав для добавления гостей. Только резиденты и администраторы могут добавлять гостей.
        </p>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit} className="max-w-md mx-auto p-6 bg-white rounded-lg shadow-md">
      <h2 className="text-2xl font-bold mb-6 text-center">Добавить гостя</h2>
      
      {error && (
        <div className="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded">
          {error}
        </div>
      )}

      <Input
        label="ФИО гостя"
        value={formData.fio}
        onChange={(value) => handleChange('fio', value)}
        placeholder="Введите ФИО гостя"
        required
      />

      <Input
        label="Email гостя"
        type="email"
        value={formData.email}
        onChange={(value) => handleChange('email', value)}
        placeholder="Введите email гостя"
        required
      />

      <Input
        label="Пароль гостя"
        type="password"
        value={formData.password}
        onChange={(value) => handleChange('password', value)}
        placeholder="Придумайте пароль для гостя"
        required
      />

      <Button
        type="submit"
        variant="primary"
        loading={loading}
        disabled={loading}
        className="w-full"
      >
        Добавить гостя
      </Button>
    </form>
  );
};