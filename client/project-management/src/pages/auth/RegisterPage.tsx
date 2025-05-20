// src/pages/auth/RegisterPage.tsx
import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { TextInput, Button, Text } from '@gravity-ui/uikit';
import { registerUserApi } from '../../shared/api/noteApi';
import './AuthPage.scss';

const RegisterPage: React.FC = () => {
  const [name, setName] = useState('');
  const [surname, setSurname] = useState('');
  const [login, setLogin] = useState('');
  const [password, setPassword] = useState('');
  const [photo, setPhoto] = useState(''); // Опционально
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setIsLoading(true);
    setError(null);
    try {
      await registerUserApi({ name, surname, login, password, photo: photo || null });
      alert('Регистрация успешна! Теперь вы можете войти.');
      navigate('/login'); // Перенаправляем на страницу входа
    } catch (err: any) {
      console.error("Ошибка регистрации:", err);
      setError(err.response?.data?.message || err.response?.data || err.message || 'Ошибка регистрации');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-container">
        <Text variant="display-1" className="auth-title">Регистрация</Text>
        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-field">
            <TextInput label="Имя:" size="l" value={name} onUpdate={setName} disabled={isLoading} />
          </div>
          <div className="form-field">
            <TextInput label="Фамилия:" size="l" value={surname} onUpdate={setSurname} disabled={isLoading} />
          </div>
          <div className="form-field">
            <TextInput label="Логин:" size="l" value={login} onUpdate={setLogin} disabled={isLoading} />
          </div>
          <div className="form-field">
            <TextInput label="Пароль:" type="password" size="l" value={password} onUpdate={setPassword} disabled={isLoading} />
          </div>
          <div className="form-field">
            <TextInput label="URL фото (необязательно):" size="l" value={photo} onUpdate={setPhoto} disabled={isLoading} />
          </div>
          {error && <Text color="danger" className="auth-error">{error}</Text>}
          <Button type="submit" view="action" size="xl" loading={isLoading} disabled={isLoading} width="max">
            Зарегистрироваться
          </Button>
        </form>
        <Text className="auth-switch">
          Уже есть аккаунт? <Link to="/login">Войти</Link>
        </Text>
      </div>
    </div>
  );
};

export default RegisterPage;
