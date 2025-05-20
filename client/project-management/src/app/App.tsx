import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link, Navigate } from 'react-router-dom';
import { ThemeProvider } from '@gravity-ui/uikit';
import '@gravity-ui/uikit/styles/styles.css';
import './index.scss'; 

import { NoteEditPage } from '../pages/note';
import ProjectDashboardPage from '../pages/project/ProjectDashboardPage';
import LoginPage from '../pages/auth/LoginPage';
import RegisterPage from '../pages/auth/RegisterPage';
import { useAuth } from '../shared/context/AuthContext'; 

const ProtectedRoute: React.FC<{ children: JSX.Element }> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return <div>Загрузка аутентификации...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  return children;
};

function App() {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <ThemeProvider theme="light">
        <div>Загрузка приложения...</div> 
      </ThemeProvider>
    );
  }

  return (
    <ThemeProvider theme="light">
      <Router>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route 
            path="/projects" 
            element={<ProtectedRoute><ProjectDashboardPage /></ProtectedRoute>} 
          />
          <Route 
            path="/notes/:noteId/edit" 
            element={<ProtectedRoute><NoteEditPage /></ProtectedRoute>} 
          />
          <Route 
            path="/" 
            element={isAuthenticated ? <Navigate to="/projects" replace /> : <Navigate to="/login" replace />} 
          />
        </Routes>
      </Router>
    </ThemeProvider>
  );
}

export default App;
