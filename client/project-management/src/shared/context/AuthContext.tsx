// src/shared/context/AuthContext.tsx
import React, { createContext, useState, useContext, useEffect, ReactNode } from 'react';
import { UserProfile } from '../api/models';
import { getAuthToken, setAuthToken as apiSetAuthToken, getCurrentUserApi, logoutUser as apiLogoutUser } from '../api/noteApi';

interface AuthContextType {
  isAuthenticated: boolean;
  user: UserProfile | null;
  isLoading: boolean;
  login: (token: string, userData: UserProfile) => void;
  logout: () => void;
  // setUserData: (userData: UserProfile | null) => void; // Если нужно обновлять данные пользователя отдельно
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(!!getAuthToken());
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    const token = getAuthToken();
    if (token) {
      setIsLoading(true);
      getCurrentUserApi()
        .then(userData => {
          setUser(userData);
          setIsAuthenticated(true);
        })
        .catch(() => {
          apiSetAuthToken(null);
          setUser(null);
          setIsAuthenticated(false);
        })
        .finally(() => {
          setIsLoading(false);
        });
    } else {
      setIsLoading(false);
    }
  }, []);

  const login = (token: string, userData: UserProfile) => {
    apiSetAuthToken(token);
    setUser(userData);
    setIsAuthenticated(true);
  };

  const logout = () => {
    apiLogoutUser();
    setUser(null);
    setIsAuthenticated(false);
  };

  return (
    <AuthContext.Provider value={{ isAuthenticated, user, isLoading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
