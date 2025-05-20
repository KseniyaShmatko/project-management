// src/shared/ui/modals/NameInputDialog.tsx
import React, { useState, useEffect } from 'react';
import { Modal, TextInput, Button, Text } from '@gravity-ui/uikit';
import './NameInputDialog.scss';

interface NameInputDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (name: string) => void;
  title: string;
  inputLabel?: string;
  placeholder?: string;
  submitButtonText?: string;
  isLoading?: boolean;
}

const NameInputDialog: React.FC<NameInputDialogProps> = ({
  isOpen,
  onClose,
  onSubmit,
  title,
  inputLabel = "Название",
  placeholder = "Введите название...",
  submitButtonText = "Создать",
  isLoading = false,
}) => {
  const [name, setName] = useState('');
  const [errorMessage, setErrorMessage] = useState<string | undefined>(undefined);

  const handleSubmit = () => {
    if (!name.trim()) {
      setErrorMessage("Название не может быть пустым.");
      return;
    }
    setErrorMessage(undefined);
    onSubmit(name);
  };

  const handleModalClose = () => {
    setName(''); 
    setErrorMessage(undefined);
    onClose();
  };
  
  useEffect(() => {
    if (!isOpen) {
        setName(''); 
        setErrorMessage(undefined);
    }
  }, [isOpen]);

  return (
    <Modal open={isOpen} onClose={handleModalClose} contentClassName="name-input-dialog-content">
      <div className="name-input-dialog__header">
        <Text variant="header-2" className="name-input-dialog__title">{title}</Text>
      </div>
      <div className="name-input-dialog__body">
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
        {errorMessage && !isLoading && <Text variant="caption-1" color="danger" className="name-input-dialog__error-text">{errorMessage}</Text>}
      </div>
      <div className="name-input-dialog__footer">
        <Button onClick={handleModalClose} view="outlined" size="l" disabled={isLoading}>
          Отмена
        </Button>
        <Button onClick={handleSubmit} view="action" size="l" loading={isLoading}>
          {submitButtonText}
        </Button>
      </div>
    </Modal>
  );
};

export default NameInputDialog;
