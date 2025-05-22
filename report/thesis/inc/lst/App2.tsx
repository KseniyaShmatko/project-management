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
