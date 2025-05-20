import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { TextInput, Button, Text } from '@gravity-ui/uikit';

import { loginUserApi } from '../../shared/api/noteApi';
import { UserProfile } from '../../shared/api/models';
import { useAuth } from '../../shared/context/AuthContext';

import './AuthPage.scss';

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
      const userProfile: UserProfile = {
        id: authResponse.id,
        login: authResponse.login,
        name: authResponse.name,
        surname: authResponse.surname,
        photo: authResponse.photo,
      };
      authLogin(authResponse.token, userProfile);
      navigate('/projects'); 
    } catch (err: any) {
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
