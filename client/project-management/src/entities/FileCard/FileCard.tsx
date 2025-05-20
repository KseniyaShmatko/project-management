// src/entities/FileCard/FileCard.tsx
import React, { useState, useRef, useEffect } from 'react';
import { Card, Text, Icon, TextInput, Button } from '@gravity-ui/uikit';
import { FileText, BranchesRightArrowRight, BranchesDown, Filmstrip, File, Pencil } from '@gravity-ui/icons'; 

import { ProjectFile, ProjectRole } from '../../shared/api/models';

import './FileCard.scss';

interface FileCardProps {
    file: ProjectFile;
    onOpenFile: (superObjectId: string) => void;
    onUpdateFileName: (fileId: number, superObjectId: string, newName: string) => Promise<void>; 
    currentUserRole: ProjectRole | null;
}

const FileCard: React.FC<FileCardProps> = ({ file, onOpenFile, onUpdateFileName, currentUserRole }) => {
    const [isEditing, setIsEditing] = useState(false);
    const [currentName, setCurrentName] = useState(file.name);
    const [editError, setEditError] = useState<string | undefined>(undefined);
    const inputRef = useRef<HTMLInputElement>(null);

    const canEditFile = currentUserRole === ProjectRole.OWNER || currentUserRole === ProjectRole.EDITOR;

    console.log('file', file)

    useEffect(() => {
        setCurrentName(file.name);
    }, [file.name]);
    
    useEffect(() => {
        if (isEditing && inputRef.current) {
        inputRef.current.focus();
        inputRef.current.select();
        }
    }, [isEditing]);
    
    const handleEditIconClick = (event: React.MouseEvent) => {
        event.stopPropagation();
        setIsEditing(true);
    };
    
    const handleNameChange = (value: string) => {
        setCurrentName(value);
        if (editError && value.trim()) setEditError(undefined);
    };
    
    const handleNameSave = async () => {
        if (!currentName.trim()) {
            setEditError("Имя не может быть пустым");
            return;
        }
        if (currentName.trim() === file.name) {
            setIsEditing(false);
            return;
        }
        try {
            await onUpdateFileName(file.id, file.superObjectId!, currentName.trim());
            setIsEditing(false);
            setEditError(undefined);
        } catch (error) {
            console.error("Ошибка обновления имени файла:", error);
            setEditError("Не удалось обновить имя.");
        }
    };
    
    const handleNameCancel = () => {
        setIsEditing(false);
        setCurrentName(file.name);
        setEditError(undefined);
    };
    
    const getFileIcon = () => {
        if (!file.type || !file.type.name) {
            return <Icon data={File} size={32} />;
        }
        
        switch (file.type.name.toLowerCase()) {
            case 'note':
                return <Icon data={FileText} size={32} />;
            case 'scheme':
                return <Icon data={BranchesDown} size={32} />;
            case 'presentation':
                return <Icon data={Filmstrip} size={32} />;
            case 'mindmap':
                return <Icon data={BranchesRightArrowRight} size={32} />;
            default:
                return <Icon data={File} size={32} />;
        }
    };
    
    const getIconClass = () => {
        if (!file.type || !file.type.name) {
            return 'file-card__icon-area--default';
        }
        
        switch (file.type.name.toLowerCase()) {
            case 'note':
                return 'file-card__icon-area--note';
            case 'scheme':
                return 'file-card__icon-area--scheme';
            case 'presentation':
                return 'file-card__icon-area--presentation';
            case 'mindmap':
                return 'file-card__icon-area--mindmap';
            default:
                return 'file-card__icon-area--default';
        }
    };

    return (
        <div onClick={!isEditing ? () => onOpenFile(file.superObjectId!) : undefined} role="button">
            <Card 
                type="container" 
                view="filled" 
                className={`file-card ${isEditing ? 'file-card--editing' : ''}`}
                role="button"
                tabIndex={isEditing ? -1 : 0}
                aria-label={`Открыть файл ${file.name}`}
            >
            <div className={`file-card__icon-area ${getIconClass()}`}> 
                {getFileIcon()}
            </div>
            <div className="file-card__info">
                {isEditing ? (
                <div className="file-card__edit-name-form">
                    <TextInput
                        value={currentName}
                        onUpdate={handleNameChange}
                        onKeyPress={(e) => { if (e.key === 'Enter') handleNameSave(); if (e.key === 'Escape') handleNameCancel(); }}
                        size="s"
                        error={editError}
                        controlRef={inputRef}
                    />
                </div>
                ) : (
                <div className="file-card__edit">
                    <Text variant="body-2" className="file-card__name" ellipsis>
                        {file.name}
                    </Text>
                    {canEditFile && !isEditing && (
                        <div className="file-card__actions">
                            <Button view="flat-secondary" size="s" onClick={handleEditIconClick} title="Редактировать имя">
                                <Icon data={Pencil} />
                            </Button>
                        </div>
                    )}
                </div>
                )}
                {(file.date) && !isEditing && (
                <Text variant="caption-2" color="secondary" className="file-card__meta">
                    Создано: {file.date ? new Date(file.date).toLocaleDateString() : ''}
                </Text>
                )}
            </div>
            {isEditing && canEditFile && (
                <div className="file-card__edit-controls">
                    <Button size="s" view="action" onClick={handleNameSave}>Ок</Button>
                    <Button size="s" view="flat" onClick={handleNameCancel}>Отменить</Button>
                </div>
            )}
            </Card>
        </div>
    );
};

export default FileCard;
