// src/features/sidebar/Sidebar.tsx
import React from 'react';
import { useNavigate } from 'react-router-dom'; // Для перенаправления после logout
import { Button, Text, Loader, User, Avatar } from '@gravity-ui/uikit'; // Добавили User и Avatar
import { Plus, ArrowRightFromSquare } from '@gravity-ui/icons'; // Иконка для выхода
import ProjectList from '../../entities/ProjectList/ProjectList'; // Убедитесь, что путь верный
import { useAuth } from '../../shared/context/AuthContext'; // Импорт хука useAuth
import './Sidebar.scss';

interface ProjectListItem { // Переименовал Project в ProjectListItem для ясности
  id: string;
  name: string;
}

interface SidebarProps {
  projects: ProjectListItem[];
  selectedProjectId: string | null;
  onSelectProject: (projectId: string) => void;
  onCreateNewProject: () => void;
  isLoading: boolean; // isLoading для списка проектов
}

const Sidebar: React.FC<SidebarProps> = ({
  projects,
  selectedProjectId,
  onSelectProject,
  onCreateNewProject,
  isLoading,
}) => {
  const { user, isAuthenticated, logout, isLoading: isAuthLoading } = useAuth(); // Получаем данные пользователя и статус
  const navigate = useNavigate();

  const handleLogout = () => {
    logout(); // Вызываем функцию logout из AuthContext
    navigate('/login'); // Перенаправляем на страницу входа
  };

  // Определяем, что отображать для пользователя
  let userDisplay = null;
  if (isAuthLoading) {
    userDisplay = <Loader size="s" />;
  } else if (isAuthenticated && user) {
    userDisplay = (
      <div className="sidebar__user-profile">
        <User
            avatar={{
                text: `${user.name.charAt(0)}${user.surname.charAt(0)}`, // Инициалы, если нет фото
            }}
            name={`${user.name} ${user.surname}`}
            // description={user.login} // Можно добавить логин, если нужно
            size="l" 
        />
        <Button view="flat" title="Выйти" onClick={handleLogout} className="sidebar__logout-button">
            <Button.Icon><ArrowRightFromSquare /></Button.Icon>
            {/* Выйти */}
        </Button>
      </div>
    );
  } else {
    // Если не авторизован (хотя на этой странице он должен быть авторизован, если это часть дашборда)
    // Можно ничего не показывать или кнопку "Войти"
    userDisplay = (
        <Button view="action" onClick={() => navigate('/login')}>Войти</Button>
    );
  }


  return (
    <div className="sidebar-container"> {/* Добавил общий контейнер для стилизации */}
      <div className="sidebar__header">
        <img src="/bmstu-logo.png" alt="Logo" className="sidebar__logo" />
        <Text variant="subheader-2" className="sidebar__app-name">МГТУ им Н.Э. Баумана</Text>
      </div>
      
      <div className="sidebar__projects-header">
        <Text variant="subheader-1" className="sidebar__projects-title">Мои проекты</Text>
        <Button view="flat-action" size="l" onClick={onCreateNewProject} title="Создать новый проект">
          <Button.Icon><Plus /></Button.Icon>
          {/* Новый */}
        </Button>
      </div>

      {isLoading ? ( // isLoading для проектов
        <div className="sidebar__loader-container">
          <Loader size="m" />
        </div>
      ) : (
        <ProjectList
          projects={projects}
          selectedProjectId={selectedProjectId}
          onSelectProject={onSelectProject}
        />
      )}
      
      {/* Профиль пользователя внизу */}
      <div className="sidebar__footer">
        {userDisplay}
      </div>
    </div>
  );
};

export default Sidebar;
