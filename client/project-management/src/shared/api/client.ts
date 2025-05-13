// src/shared/api/client.ts
import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'http://localhost:8080', // УКАЖИ АДРЕС ТВОЕГО БЭКЕНДА
  headers: {
    'Content-Type': 'application/json',
    // Здесь можно будет добавлять заголовки для аутентификации (JWT токен)
  },
});

// Можно добавить интерсепторы для обработки ошибок или токенов
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // Обработка ошибок (например, вывод в консоль, показ уведомлений)
    console.error('API Error:', error.response?.data || error.message);
    return Promise.reject(error);
  }
);

export default apiClient;
