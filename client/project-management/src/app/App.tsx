import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { NoteEditPage } from '../pages/note'; // Импортируем страницу
import './index.scss'; // Предположим, у нас есть глобальные стили

function App() {
  console.log("App component is rendering");
  return (
    <Router>
      <div className="app">
        <Routes>
          {/* Пока открываем страницу редактирования по определенному ID */}
          {/* В будущем ID будет динамическим */}
          <Route path="/notes/:noteId/edit" element={<NoteEditPage />} />
          {/* Можно добавить главную страницу или другие роуты позже */}
          <Route path="/" element={<div>Главная страница (заглушка)</div>} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
