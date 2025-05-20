// src/pages/auth/LoginPage.tsx
import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { TextInput, Button, Text } from '@gravity-ui/uikit';

import { loginUserApi, setAuthToken } from '../../shared/api/noteApi'; // Импортируем API
import { UserProfile } from '../../shared/api/models'; // Для установки токена
import { useAuth } from '../../shared/context/AuthContext';

import './AuthPage.scss'; // Общие стили для страниц Auth

const LoginPage: React.FC = () => {
  const [login, setLogin] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  const { login: authLogin } = useAuth();

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setIsLoading(true);
    setError(null);
    try {
      const authResponse = await loginUserApi({ login, password });
      // authResponse содержит { token, id, login, name, surname, photo }
      const userProfile: UserProfile = { // Формируем UserProfile из AuthResponse
        id: authResponse.id,
        login: authResponse.login,
        name: authResponse.name,
        surname: authResponse.surname,
        photo: authResponse.photo,
      };
      authLogin(authResponse.token, userProfile); // Вызываем login из AuthContext
      // loginUserApi уже вызвал setAuthToken, так что можно было бы не передавать токен в authLogin,
      // а чтобы authLogin сам его брал или доверял, что он уже установлен.
      // Но для явности передаем.
      navigate('/projects'); 
    } catch (err: any) {
      // ... обработка ошибок ...
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-container">
        <Text variant="display-1" className="auth-title">Вход</Text>
        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-field">
            <TextInput
              label="Логин:"
              size="l"
              value={login}
              onUpdate={(val) => setLogin(val)}
              disabled={isLoading}
            />
          </div>
          <div className="form-field">
            <TextInput
              label="Пароль:"
              type="password"
              size="l"
              value={password}
              onUpdate={(val) => setPassword(val)}
              disabled={isLoading}
            />
          </div>
          {error && <Text color="danger" className="auth-error">{error}</Text>}
          <Button type="submit" view="action" size="xl" loading={isLoading} disabled={isLoading} width="max">
            Войти
          </Button>
        </form>
        <Text className="auth-switch">
          Нет аккаунта? <Link to="/register">Зарегистрироваться</Link>
        </Text>
      </div>
    </div>
  );
};

export default LoginPage;
