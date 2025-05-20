import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Text, Loader, User } from '@gravity-ui/uikit';
import { Plus, ArrowRightFromSquare } from '@gravity-ui/icons';

import ProjectList from '../../entities/ProjectList/ProjectList';
import { useAuth } from '../../shared/context/AuthContext';

import './Sidebar.scss';

interface ProjectListItem { 
  id: string;
  name: string;
}

interface SidebarProps {
  projects: ProjectListItem[];
  selectedProjectId: string | null;
  onSelectProject: (projectId: string) => void;
  onCreateNewProject: () => void;
  isLoading: boolean;
}

const Sidebar: React.FC<SidebarProps> = ({
  projects,
  selectedProjectId,
  onSelectProject,
  onCreateNewProject,
  isLoading,
}) => {
  const { user, isAuthenticated, logout, isLoading: isAuthLoading } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  let userDisplay = null;
  if (isAuthLoading) {
    userDisplay = <Loader size="s" />;
  } else if (isAuthenticated && user) {
    userDisplay = (
      <div className="sidebar__user-profile">
        <User
            avatar={{
                text: `${user.name.charAt(0)}${user.surname.charAt(0)}`,
            }}
            name={`${user.name} ${user.surname}`}
            size="l" 
        />
        <Button view="flat" title="Выйти" onClick={handleLogout} className="sidebar__logout-button">
            <Button.Icon><ArrowRightFromSquare /></Button.Icon>
        </Button>
      </div>
    );
  } else {
    userDisplay = (
        <Button view="action" onClick={() => navigate('/login')}>Войти</Button>
    );
  }


  return (
    <div className="sidebar-container">
      <div className="sidebar__header">
        <img src="/bmstu-logo.png" alt="Logo" className="sidebar__logo" />
        <Text variant="subheader-2" className="sidebar__app-name">МГТУ им Н.Э. Баумана</Text>
      </div>
      
      <div className="sidebar__projects-header">
        <Text variant="subheader-1" className="sidebar__projects-title">Мои проекты</Text>
        <Button view="flat-action" size="l" onClick={onCreateNewProject} title="Создать новый проект">
          <Button.Icon><Plus /></Button.Icon>
        </Button>
      </div>

      {isLoading ? (
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
      <div className="sidebar__footer">
        {userDisplay}
      </div>
    </div>
  );
};

export default Sidebar;
