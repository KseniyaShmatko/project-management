import React from 'react';
import { useParams } from 'react-router-dom';
import { NoteEditorWidget } from '../../../widgets/note-editor'; // Импортируем виджет
import './NoteEditPage.scss'; // Стили для страницы

export const NoteEditPage: React.FC = () => {
  const { noteId } = useParams<{ noteId: string }>(); // Получаем ID заметки из URL

  // Пока просто выводим ID, в будущем будем его использовать для загрузки данных
  console.log('Editing note with ID:', noteId);

  return (
    <div className="note-edit-page">
      {/* Можно добавить заголовок страницы или другую информацию */}
      <h1>Редактирование заметки {noteId ? `№${noteId}` : ''}</h1>
      <NoteEditorWidget noteId={noteId} />
    </div>
  );
};
