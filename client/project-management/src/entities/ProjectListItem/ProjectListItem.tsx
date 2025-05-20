// src/entities/ProjectListItem/ProjectListItem.tsx
import React from 'react';
import { Text } from '@gravity-ui/uikit';

import './ProjectListItem.scss';

interface Project {
  id: string;
  name: string;
}

interface ProjectListItemProps {
  project: Project;
  isSelected: boolean;
  onSelect: (projectId: string) => void;
}

const ProjectListItem: React.FC<ProjectListItemProps> = ({ project, isSelected, onSelect }) => {
  const handleClick = () => {
    onSelect(project.id);
  };

  return (
    <div
      className={`project-list-item ${isSelected ? 'project-list-item--selected' : ''}`}
      onClick={handleClick}
      role="button"
      tabIndex={0}
      onKeyPress={(e) => { if (e.key === 'Enter' || e.key === ' ') handleClick(); }}
    >
      <Text variant="body-1" className="project-list-item__name" ellipsis>
        {project.name}
      </Text>
    </div>
  );
};

export default ProjectListItem;
