import React, { useState, useRef, useEffect } from 'react';
import { Button, Text, Loader, TextInput, Icon } from '@gravity-ui/uikit';
import { Plus, Pencil, Gear } from '@gravity-ui/icons';

import { Project, ProjectFile, ProjectRole } from '../../shared/api/models';

import FileGrid from '../../entities/FileGrid/FileGrid';

import './MainArea.scss';

interface MainAreaProps {
    selectedProject?: Project | null;
    files: ProjectFile[];
    onOpenFile: (superObjectId: string) => void;
    onCreateNewFile: () => void;
    isLoading: boolean;
    onProjectNameUpdate: (projectId: number, newName: string) => Promise<void>;
    onUpdateFileName: (fileId: number, superObjectId: string, newName: string) => Promise<void>; 
    onOpenAccessModal: () => void;
}

const MainArea: React.FC<MainAreaProps> = ({
  selectedProject,
  files,
  onOpenFile,
  onCreateNewFile,
  isLoading,
  onProjectNameUpdate,
  onUpdateFileName,
  onOpenAccessModal,
}) => {
    const [isEditingProjectName, setIsEditingProjectName] = useState(false);
    const [newProjectName, setNewProjectName] = useState('');
    const [projectNameError, setProjectNameError] = useState<string | undefined>(undefined);
