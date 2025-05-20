//src/pages/note/ui/NoteEditPage.tsx
import React, { useState, useEffect } from 'react';
import { useParams, useLocation } from 'react-router-dom';

import { NoteEditorWidget } from '../../../widgets/note-editor'; 
import { ProjectRole } from '../../../shared/api/models'

import './NoteEditPage.scss';

export const NoteEditPage: React.FC = () => {
    const { noteId } = useParams<{ noteId: string }>();
    const location = useLocation(); 

    const passedState = location.state as { currentUserRole?: ProjectRole | null, projectId?: number | null } | null;
    const initialCurrentUserRole = passedState?.currentUserRole || null;
    const initialProjectId = passedState?.projectId || null; // ID проекта, если передан

    const readOnly = !(initialCurrentUserRole === ProjectRole.OWNER || initialCurrentUserRole === ProjectRole.EDITOR);
    console.log(noteId, initialCurrentUserRole)
    return (
      <div className="note-edit-page">
        {noteId ? (
          <NoteEditorWidget 
            key={noteId} 
            noteId={noteId} 
            readOnly={readOnly} 
            // projectId={projectId} // Можно передать projectId дальше, если нужно NEditoWidg
          />
        ) : (
          <p>ID заметки не указан.</p>
        )}
      </div>
    );
};
