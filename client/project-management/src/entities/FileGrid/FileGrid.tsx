// src/entities/FileGrid/FileGrid.tsx
import React from 'react';
import { Text } from '@gravity-ui/uikit';

import FileCard from '../FileCard/FileCard';
import { ProjectFile, ProjectRole } from '../../shared/api/models';

import './FileGrid.scss';

interface FileGridProps {
    files: ProjectFile[];
    onOpenFile: (superObjectId: string) => void;
    onUpdateFileName: (fileId: number, superObjectId: string, newName: string) => Promise<void>; 
    currentUserRole: ProjectRole | null;
}

const FileGrid: React.FC<FileGridProps> = ({ files, onOpenFile, onUpdateFileName, currentUserRole }) => {
  if (files.length === 0) {
    return (
      <div className="file-grid file-grid--empty">
        <Text color="secondary">В этом проекте пока нет файлов.</Text>
      </div>
    );
  }

  return (
    <div className="file-grid">
      {files.map((file) => (
        <FileCard 
          key={file.superObjectId || file.id}
          file={file} 
          onOpenFile={onOpenFile}
          onUpdateFileName={onUpdateFileName} 
          currentUserRole={currentUserRole}
        />
      ))}
    </div>
  );
};

export default FileGrid;
