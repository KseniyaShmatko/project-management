import React, { useState, useEffect } from 'react';
import { Modal, TextInput, Button, Text, Select } from '@gravity-ui/uikit';
import './NameInputDialog.scss';
import { FileType } from '../../api/models';

interface NameInputDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (name: string, fileTypeId?: number) => void;
  title: string;
  inputLabel?: string;
  placeholder?: string;
  submitButtonText?: string;
  isLoading?: boolean;
  showFileTypeSelect?: boolean;
  fileTypes?: FileType[];
}

const fileTypeTranslations: Record<string, string> = {
  'note': 'Заметка',
  'scheme': 'Схема',
  'presentation': 'Презентация',
  'mindmap': 'Mind-map',
  'chat': 'Чат',
  'calendar': 'Календарь',
  'tracker': 'Трекер',
  // Добавьте другие типы по мере необходимости
};

const getFileTypeTranslation = (typeName: string): string => {
  const normalizedType = typeName.toLowerCase();
  return fileTypeTranslations[normalizedType] || typeName; // Если перевода нет, возвращаем оригинальное название
};

const NameInputDialog: React.FC<NameInputDialogProps> = ({
  isOpen,
  onClose,
  onSubmit,
  title,
  inputLabel = "Название",
  placeholder = "Введите название...",
  submitButtonText = "Создать",
  isLoading = false,
  showFileTypeSelect = false,
  fileTypes = [],
}) => {
  const [name, setName] = useState('');
  const [selectedFileTypeId, setSelectedFileTypeId] = useState<number | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | undefined>(undefined);

  useEffect(() => {
    if (showFileTypeSelect && fileTypes.length > 0) {
      // Установка типа 'note' по умолчанию, если он существует
      const noteType = fileTypes.find(type => type.name.toLowerCase() === 'note');
      if (noteType) {
        setSelectedFileTypeId(noteType.id);
      }
    }
  }, [fileTypes, showFileTypeSelect]);

  const handleSubmit = () => {
    if (!name.trim()) {
      setErrorMessage("Название не может быть пустым.");
      return;
    }
    
    if (showFileTypeSelect && selectedFileTypeId === null) {
      setErrorMessage("Выберите тип файла.");
      return;
    }
    
    setErrorMessage(undefined);
    onSubmit(name, showFileTypeSelect ? selectedFileTypeId! : undefined);
  };

  const handleModalClose = () => {
    setName(''); 
    setSelectedFileTypeId(null);
    setErrorMessage(undefined);
    onClose();
  };
  
  useEffect(() => {
    if (!isOpen) {
        setName(''); 
        setSelectedFileTypeId(null);
        setErrorMessage(undefined);
    }
  }, [isOpen]);

  return (
    <Modal open={isOpen} onClose={handleModalClose} contentClassName="name-input-dialog-content">
      <div>
        <Text variant="header-2" className="name-input-dialog__title">{title}</Text>
      </div>
      <div className="name-input-dialog__form">
        <TextInput
          label={inputLabel}
          value={name}
          onUpdate={(value) => {
            setName(value);
            if (errorMessage && value.trim()) {
              setErrorMessage(undefined);
            }
          }}
          placeholder={placeholder}
          disabled={isLoading}
          error={errorMessage}
          autoFocus
          size="l"
        />
        
        {showFileTypeSelect && fileTypes.length > 0 && (
          <div className="name-input-dialog__file-type-select">
            <Select
              label="Тип файла"
              value={selectedFileTypeId ? [selectedFileTypeId.toString()] : []}
              options={fileTypes.map(type => ({
                value: type.id.toString(),
                content: getFileTypeTranslation(type.name),
                disabled: type.name.toLowerCase() !== 'note'
              }))}
              onUpdate={(values) => {
                if (values.length > 0) {
                  setSelectedFileTypeId(parseInt(values[0], 10));
                  setErrorMessage(undefined);
                }
              }}
              disabled={isLoading}
              size="l"
            />
            {selectedFileTypeId && fileTypes.find(type => type.id === selectedFileTypeId)?.name.toLowerCase() !== 'note' && (
              <Text variant="caption-1" color="info" className="name-input-dialog__helper-text">
                В настоящее время разрешено создавать только заметки (note)
              </Text>
            )}
          </div>
        )}
        
        {errorMessage && !isLoading && <Text variant="caption-1" color="danger" className="name-input-dialog__error-text">{errorMessage}</Text>}
      </div>
      <div className="name-input-dialog__buttons">
        <Button onClick={handleModalClose} view="outlined" size="l" disabled={isLoading}>
          Отмена
        </Button>
        <Button onClick={handleSubmit} className="name-input-dialog__button_ok" view="action" size="l" loading={isLoading}>
          {submitButtonText}
        </Button>
      </div>
    </Modal>
  );
};

export default NameInputDialog;
