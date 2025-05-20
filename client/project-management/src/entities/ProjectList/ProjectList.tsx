import React from 'react';
import { Text } from '@gravity-ui/uikit';

import ProjectListItem from '../ProjectListItem/ProjectListItem';

import './ProjectList.scss';

interface Project {
  id: string;
  name: string;
}

interface ProjectListProps {
  projects: Project[];
  selectedProjectId: string | null;
  onSelectProject: (projectId: string) => void;
}

const ProjectList: React.FC<ProjectListProps> = ({ projects, selectedProjectId, onSelectProject }) => {
  if (!projects || projects.length === 0) {
    return (
      <div className="project-list project-list--empty">
        <Text color="secondary">Проектов пока нет.</Text>
      </div>
    );
  }

  return (
    <div className="project-list">
      {projects.map((project) => (
        <ProjectListItem
          key={project.id}
          project={project}
          isSelected={project.id === selectedProjectId}
          onSelect={onSelectProject}
        />
      ))}
    </div>
  );
};

export default ProjectList;
