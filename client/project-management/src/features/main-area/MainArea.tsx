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
    const projectNameInputRef = useRef<HTMLInputElement>(null);

    const canEditProject = selectedProject?.currentUserRole === ProjectRole.OWNER || selectedProject?.currentUserRole === ProjectRole.EDITOR;
    const isOwner = selectedProject?.currentUserRole === ProjectRole.OWNER;

    useEffect(() => {
        if (selectedProject) {
            setNewProjectName(selectedProject.name);
            setIsEditingProjectName(false);
        }
    }, [selectedProject]);
    
    useEffect(() => {
        if (isEditingProjectName && projectNameInputRef.current) {
            projectNameInputRef.current.focus();
            projectNameInputRef.current.select();
        }
    }, [isEditingProjectName]);
    
    const handleProjectNameEditStart = () => {
        if (selectedProject) {
            setNewProjectName(selectedProject.name);
            setIsEditingProjectName(true);
        }
    };
    
    const handleProjectNameChange = (value: string) => {
        setNewProjectName(value);
        if (projectNameError && value.trim()) {
            setProjectNameError(undefined);
        }
    };
    
    const handleProjectNameSave = async () => {
        if (!selectedProject || !newProjectName.trim()) { return; }
        if (newProjectName.trim() === selectedProject.name) { return; }
    
        try {
          await onProjectNameUpdate(selectedProject.id, newProjectName.trim());
          setIsEditingProjectName(false);
          setProjectNameError(undefined);
        } catch (error) {
          console.error("Ошибка обновления имени проекта (в MainArea):", error);
          setProjectNameError('Не удалось обновить имя проекта.');
        }
      };
    
    const handleProjectNameCancel = () => {
        setIsEditingProjectName(false);
        setProjectNameError(undefined);
        if (selectedProject) {
            setNewProjectName(selectedProject.name);
        }
    };
    
    if (!selectedProject) {
        return (
        <div className="main-area main-area--empty">
            <Text variant="header-1" className="main-area__placeholder-title">Выберите или создайте проект</Text>
            <Text variant="body-1" color="secondary">
            Чтобы начать работу, выберите проект из списка слева или создайте новый.
            </Text>
        </div>
        );
    }

    return (
        <div className="main-area">
          <header className="main-area__header">
            {isEditingProjectName && canEditProject ? (
              <div className="main-area__project-name-edit">
                <TextInput
                  value={newProjectName}
                  onUpdate={handleProjectNameChange}
                  onKeyPress={(e) => { if (e.key === 'Enter') handleProjectNameSave(); if (e.key === 'Escape') handleProjectNameCancel(); }}
                  size="xl"
                  error={projectNameError}
                  controlRef={projectNameInputRef}
                />
                <Button view="action" onClick={handleProjectNameSave}>Сохранить</Button>
                <Button view="flat" onClick={handleProjectNameCancel}>Отмена</Button>
              </div>
            ) : (
              <div className="main-area__project-name-display" onClick={handleProjectNameEditStart} title="Редактировать имя проекта">
                <Text variant="header-1" className="main-area__project-title">{selectedProject.name}</Text>
                {canEditProject ? (
                  <Button view="flat-secondary" className="main-area__edit-icon-button">
                    <Icon data={Pencil} size={18} />
                  </Button>
                ) : null}
              </div>
            )}
            {selectedProject.currentUserRole && (
                    <Text variant="caption-1" color="secondary" className="main-area__current-role">
                        Ваша роль: {selectedProject.currentUserRole === ProjectRole.OWNER ? "Владелец" : selectedProject.currentUserRole}
                    </Text>
            )}
            <div className="main-area__actions">
                {(canEditProject) && (
                    <Button view="action" size="l" onClick={onCreateNewFile}>
                        <Button.Icon><Plus /></Button.Icon>
                        Создать файл
                    </Button>
                )}
                {isOwner && (
                    <Button view="outlined" size="l" onClick={onOpenAccessModal} title="Управление доступом">
                        <Icon data={Gear} />
                    </Button>
                )}
            </div>
          </header>
        
        {isLoading ? (
            <div className="main-area__loader">
            <Loader size="l" />
            </div>
        ) : (
            <FileGrid files={files} onOpenFile={onOpenFile} onUpdateFileName={onUpdateFileName} currentUserRole={selectedProject.currentUserRole}/>
        )}
        </div>
    );
};

export default MainArea;
