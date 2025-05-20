// src/features/project-access-modal/ProjectAccessModal.tsx
import React, { useState, useEffect, useCallback } from 'react';
import {
  Modal,
  Button,
  Text,
  Select,
  Avatar,
  Icon,
  TextInput,
  Loader,
  User, // Для отображения участников
} from '@gravity-ui/uikit';
import { PersonPlus, TrashBin, ArrowDown, ChevronDown } from '@gravity-ui/icons'; // Иконки
import { debounce } from 'lodash-es';

import { ProjectParticipant, UserProfile, ProjectRole, ProjectUserActionPayload } from '../../../shared/api/models';
import {
  searchUsersByLoginApi,
  // getProjectParticipantsApi, // Участники будут передаваться через props
  linkUserToProjectApi,
  updateUserProjectRoleApi,
  removeUserFromProjectApi
} from '../../../shared/api/noteApi'; // Предполагаем, что эти функции есть
import './ProjectAccessModal.scss';

interface ProjectAccessModalProps {
  isOpen: boolean;
  onClose: () => void;
  project: { id: number; name: string; participants: ProjectParticipant[]; owner: { id: number } | null } | null; // Передаем текущих участников
  onParticipantsUpdate: (updatedParticipants: ProjectParticipant[]) => void; // Коллбэк для обновления списка участников на странице
  currentUserId: number; // ID текущего авторизованного пользователя
}

const ProjectAccessModal: React.FC<ProjectAccessModalProps> = ({
  isOpen,
  onClose,
  project,
  onParticipantsUpdate,
  currentUserId,
}) => {
  const [participants, setParticipants] = useState<ProjectParticipant[]>(project?.participants || []);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchedUsers, setSearchedUsers] = useState<UserProfile[]>([]);
  const [selectedUser, setSelectedUser] = useState<UserProfile | null>(null);
  const [selectedRole, setSelectedRole] = useState<ProjectRole>(ProjectRole.VIEWER);
  const [isLoadingSearch, setIsLoadingSearch] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (project) {
      setParticipants(project.participants || []);
    } else {
      setParticipants([]);
    }
  }, [project]);

  console.log('searchedUsers',searchedUsers)
  console.log('participants',participants)
  const debouncedSearch = useCallback(
    debounce(async (query: string) => {
      if (query.trim().length < 2) {
        setSearchedUsers([]);
        setIsLoadingSearch(false);
        return;
      }
      setIsLoadingSearch(true);
      try {
        const users = await searchUsersByLoginApi(query);
        // Исключаем тех, кто уже участник, и самого себя (если не OWNER для возможности изменения своей роли, но это редкий кейс)
        const currentParticipantIds = participants.map(p => p.userId);
        setSearchedUsers(users.filter(u => !currentParticipantIds.includes(u.id) && u.id !== currentUserId ));
      } catch (e) {
        console.error('Ошибка поиска пользователей:', e);
        setSearchedUsers([]);
      } finally {
        setIsLoadingSearch(false);
      }
    }, 500),
    [participants, currentUserId]
  );

  useEffect(() => {
    debouncedSearch(searchQuery);
  }, [searchQuery, debouncedSearch]);


  const handleAddParticipant = async () => {
    if (!project || !selectedUser) {
      setError("Пользователь не выбран.");
      return;
    }
    setIsSubmitting(true);
    setError(null);
    try {
      const payload: ProjectUserActionPayload = {
        projectId: project.id,
        userId: selectedUser.id,
        role: selectedRole,
      };
      const newParticipantLink = await linkUserToProjectApi(payload);
      // Обновляем список участников
      const updatedParticipantsList = [...participants, {
        id: newParticipantLink.id, // Бэкенд должен вернуть ProjectParticipant или ProjectUserView
        projectId: project.id,
        userId: selectedUser.id,
        login: selectedUser.login,
        name: selectedUser.name,
        surname: selectedUser.surname,
        photo: selectedUser.photo,
        role: selectedRole
      }];
      setParticipants(updatedParticipantsList);
      onParticipantsUpdate(updatedParticipantsList); // Уведомляем родителя
      setSelectedUser(null);
      setSearchQuery('');
      setSearchedUsers([]);
      setSelectedRole(ProjectRole.VIEWER);
    } catch (e: any) {
      console.error('Ошибка добавления участника:', e);
      setError(e.response?.data?.message || e.message || "Не удалось добавить участника.");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleRoleChange = async (participantUserId: number, newRole: ProjectRole) => {
    if (!project) return;
    // Не позволяем менять роль владельца проекта через этот интерфейс
    if (participantUserId === project.owner?.id && newRole !== ProjectRole.OWNER) {
        alert("Роль владельца проекта не может быть изменена.");
        return;
    }
    if (newRole === ProjectRole.OWNER && participantUserId !== project.owner?.id) {
        alert("Роль OWNER может быть только у создателя проекта.");
        return;
    }

    const originalParticipants = [...participants];
    // Оптимистичное обновление
    const updatedOptimistic = participants.map(p =>
        p.userId === participantUserId ? { ...p, role: newRole } : p
    );
    setParticipants(updatedOptimistic);

    try {
        await updateUserProjectRoleApi(project.id, participantUserId, newRole);
        onParticipantsUpdate(updatedOptimistic); // Уведомляем родителя об успешном обновлении
    } catch (e: any) {
        console.error('Ошибка изменения роли:', e);
        setError(e.response?.data?.message || e.message || "Не удалось изменить роль.");
        setParticipants(originalParticipants); // Откат
    }
  };

  const handleRemoveParticipant = async (participantUserId: number) => {
    if (!project || participantUserId === project.owner?.id) {
      alert("Владельца проекта нельзя удалить.");
      return;
    }
    if (!window.confirm("Вы уверены, что хотите удалить этого пользователя из проекта?")) return;

    setIsSubmitting(true); // Можно использовать отдельный лоадер для строки
    try {
      await removeUserFromProjectApi(project.id, participantUserId);
      const filteredParticipants = participants.filter(p => p.userId !== participantUserId);
      setParticipants(filteredParticipants);
      onParticipantsUpdate(filteredParticipants);
    } catch (e: any) {
      console.error('Ошибка удаления участника:', e);
      setError(e.response?.data?.message || e.message || "Не удалось удалить участника.");
    } finally {
        setIsSubmitting(false);
    }
  };


  const renderParticipant = (participant: ProjectParticipant) => (
    <div key={participant.userId} className="project-access-modal__participant-row">
      <User
        avatar={{ 
            text: `${participant.name?.charAt(0)}${participant.surname?.charAt(0)}`
        }}
        name={`${participant.name || ''} ${participant.surname || ''}`}
        description={participant.login}
        className="project-access-modal__user-info"
      />
      <div className="project-access-modal__role-controls">
        {project?.owner?.id === participant.userId ? (
          <Text className="project-access-modal__owner-role" color="positive">{participant.role === ProjectRole.OWNER ? "Владелец" : participant.role}</Text>
        ) : (
          <Select
            value={[participant.role]}
            onUpdate={(value) => handleRoleChange(participant.userId, value[0] as ProjectRole)}
            options={Object.values(ProjectRole)
                .filter(role => role !== ProjectRole.OWNER) // Нельзя назначить OWNER через селект
                .map(role => ({ value: role, content: role }))
            }
            size="s"
            disabled={currentUserId !== project?.owner?.id} // Только владелец может менять роли
          />
        )}
      </div>
      {currentUserId === project?.owner?.id && participant.userId !== project?.owner?.id && (
         <Button
            view="flat-danger"
            size="s"
            onClick={() => handleRemoveParticipant(participant.userId)}
            title="Удалить участника"
            className="project-access-modal__remove-button"
        >
            <Icon data={TrashBin} />
        </Button>
      )}
    </div>
  );
  
  // Только владелец проекта может добавлять новых участников
  const canManageParticipants = currentUserId === project?.owner?.id;
  const valueForSelect = selectedUser ? [selectedUser.login] : [];

  return (
    <Modal open={isOpen} onClose={onClose} contentClassName="project-access-modal-content">
      <div className="project-access-modal__header">
        <Text variant="header-2">Управление доступом к проекту "{project?.name}"</Text>
      </div>

      <div className="project-access-modal__participants-list">
        <Text variant="subheader-1" className="project-access-modal__list-title">Участники:</Text>
        {participants.length > 0 ? (
          participants.map(renderParticipant)
        ) : (
          <Text color="secondary">В проекте пока нет других участников.</Text>
        )}
      </div>

      {canManageParticipants && (
        <div className="project-access-modal__add-participant-section">
          <Text variant="subheader-1" className="project-access-modal__add-title">Добавить участника:</Text>
          <Select
            filterable
            value={valueForSelect} // Передаем массив с одним логином
            onUpdate={(newValues) => { // newValues будет типа ["some_login"]
                const login = newValues[0];
                console.log('onUpdate - selected login:', login);
                const user = searchedUsers.find(u => u.login === login);
                console.log('onUpdate - found user:', user);
                setSelectedUser(user || null);
            }}
            onFilterChange={setSearchQuery}
            renderOption={(option) => ( // option.data это UserProfile
                <User
                    name={`${option.data?.name || ''} ${option.data?.surname || ''}`.trim()}
                    description={option.data?.login}
                />
            )}
            renderSelectedOption={(option) => { // option должен быть полным объектом {value, content, data}
                return <span>{option.value}</span>;
            }}
            options={searchedUsers.map(u => ({ 
                value: u.login, 
                content: `${u.name} ${u.surname} (${u.login})`, 
                data: u 
            }))}
            placeholder="Начните вводить логин для поиска..."
            loading={isLoadingSearch}
            className="project-access-modal__user-select"
        />
          <Select
            value={[selectedRole]}
            onUpdate={(value) => setSelectedRole(value[0] as ProjectRole)}
            options={Object.values(ProjectRole)
                .filter(role => role !== ProjectRole.OWNER) // Нельзя назначить OWNER
                .map(role => ({ value: role, content: role }))
            }
            className="project-access-modal__role-select-new"
            disabled={!selectedUser}
          />
          <Button 
            onClick={handleAddParticipant} 
            view="action" 
            loading={isSubmitting} 
            disabled={!selectedUser || isSubmitting}
            className="project-access-modal__add-button"
          >
            <Icon data={PersonPlus} /> Добавить
          </Button>
          {error && <Text color="danger" className="project-access-modal__error-text">{error}</Text>}
        </div>
      )}

      <div className="project-access-modal__footer">
        <Button onClick={onClose} size="l">Закрыть</Button>
      </div>
    </Modal>
  );
};

export default ProjectAccessModal;
