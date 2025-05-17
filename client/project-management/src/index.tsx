import React from 'react';
import ReactDOM from 'react-dom/client'; // Используем новый createRoot API
import './app/index.scss'; // Путь к глобальным стилям (поправил, чтобы был в app)
import { App } from './app'; // Импорт главного компонента App

// Находим корневой элемент в HTML
const rootElement = document.getElementById('root');

// Проверяем, что элемент найден, чтобы TypeScript не ругался
if (!rootElement) {
  throw new Error("Failed to find the root element. Ensure there's an element with id='root' in your HTML.");
}

// Создаем корень для рендеринга приложения
const root = ReactDOM.createRoot(rootElement);

// Рендерим компонент App в этот корень
root.render(
  // <React.StrictMode>
    <App />
  // </React.StrictMode>
);
