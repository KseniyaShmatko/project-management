import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';

import Sidebar from '../../features/sidebar/Sidebar';
import MainArea from '../../features/main-area/MainArea';

import NameInputDialog from '../../shared/ui/modals/NameInputDialog';
import ProjectAccessModal from '../../shared/ui/modals/ProjectAccessModal';
import {
  Project,
  ProjectFile,
  FileType,
  FileMetadata,
  SuperObject,
  CreateProjectPayload,
  UpdateProjectPayload,
  FileCreateDto,
  ProjectParticipant
} from '../../shared/api/models';
import {
  getAllProjectsApi,
  createProjectApi,
  updateProjectApi,
  getFilesForProjectApi,
  createFileMetadataApi,
  updateFileName,
  linkFileToProjectApi,
  getAllFileTypesApi, 
  updateFileSuperObjectId,
  createSuperObject,
  updateSuperObject,
} from '../../shared/api/noteApi'; 
import { useAuth } from '../../shared/context/AuthContext';

import './ProjectDashboardPage.scss';

const ProjectDashboardPage: React.FC = () => {
  const [projects, setProjects] = useState<Project[]>([]);
  const [selectedProjectId, setSelectedProjectId] = useState<number | null>(null);
  const [files, setFiles] = useState<ProjectFile[]>([]);
  const [fileTypes, setFileTypes] = useState<FileType[]>([]);

  const [isLoadingProjects, setIsLoadingProjects] = useState<boolean>(true);
  const [isLoadingFiles, setIsLoadingFiles] = useState<boolean>(false);
  const [isCreateProjectModalOpen, setIsCreateProjectModalOpen] = useState(false);
  const [isCreateFileModalOpen, setIsCreateFileModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isAccessModalOpen, setIsAccessModalOpen] = useState(false);

  const navigate = useNavigate();
  const { user } = useAuth();

  useEffect(() => {
    setIsLoadingProjects(true);
    Promise.all([getAllProjectsApi(), getAllFileTypesApi()])
      .then(([fetchedProjects, fetchedFileTypes]) => {
        setProjects(fetchedProjects);
        setFileTypes(fetchedFileTypes);
      })
      .catch(error => {
        console.error("Ошибка загрузки начальных данных:", error);
      })
      .finally(() => setIsLoadingProjects(false));
  }, []);

  useEffect(() => {
    if (selectedProjectId !== null) {
      setIsLoadingFiles(true);
      setFiles([]);
      
      getFilesForProjectApi(selectedProjectId)
        .then(fetchedProjectFiles => {
          const filteredFiles = fetchedProjectFiles.filter(f => f.superObjectId != null);
          setFiles(filteredFiles);
        })
        .catch(error => {
          console.error(`Ошибка загрузки файлов для проекта ${selectedProjectId}:`, error);
        })
        .finally(() => setIsLoadingFiles(false));
    } else {
      setFiles([]);
    }
  }, [selectedProjectId]);

  const currentProjectDetails = projects.find(p => p.id === selectedProjectId);

  const handleOpenAccessModal = () => setIsAccessModalOpen(true);
  const handleCloseAccessModal = () => setIsAccessModalOpen(false);

  const handleParticipantsUpdate = useCallback((updatedParticipants: ProjectParticipant[]) => {
    if (selectedProjectId !== null) {
        setProjects(prevProjects => 
            prevProjects.map(p => 
                p.id === selectedProjectId 
                ? { ...p, participants: updatedParticipants } 
                : p
            )
        );
    }
  }, [selectedProjectId]);
  
  const handleSelectProject = (projectIdString: string) => {
    setSelectedProjectId(parseInt(projectIdString, 10));
  };

  const handleOpenCreateProjectModal = () => setIsCreateProjectModalOpen(true);
  const handleCloseCreateProjectModal = () => setIsCreateProjectModalOpen(false);

  const handleOpenCreateFileModal = () => {
    if (selectedProjectId === null) return;
    setIsCreateFileModalOpen(true);
  };
  const handleCloseCreateFileModal = () => setIsCreateFileModalOpen(false);

  const handleSubmitNewProject = async (projectName: string) => {
    if (!user) {
      alert("Пользователь не авторизован!");
      return;
    }
    setIsSubmitting(true);
    try {
      const newProjectPayload: CreateProjectPayload = { name: projectName };
      const newProjectFromApi = await createProjectApi(newProjectPayload);
      setProjects(prev => [...prev, newProjectFromApi]);
      setSelectedProjectId(newProjectFromApi.id);
      handleCloseCreateProjectModal();
    } catch (apiError: any) {
      console.error("Ошибка создания проекта:", apiError);
      alert(`Ошибка создания проекта: ${apiError.response?.data?.message || apiError.message || 'Неизвестная ошибка'}`);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleSubmitNewFile = async (fileName: string) => {
    if (selectedProjectId === null || !user) {
      alert("Проект не выбран или пользователь не авторизован!");
      return;
    }
    const currentSelectedProject = projects.find(p => p.id === selectedProjectId);
    if (!currentSelectedProject) {
        alert("Не удалось определить детали текущего проекта для установки прав.");
        return;
    }
    setIsSubmitting(true);
    try {
      const documentFileType = fileTypes.find(ft => 
        ft.name.toLowerCase() === 'note'
      );
      if (!documentFileType) {
        alert("Системный тип файла 'document'/'note' не найден. Обратитесь к администратору.");
        setIsSubmitting(false); return;
      }

      const newFileDto: FileCreateDto = { 
        name: fileName, 
        typeId: documentFileType.id,
        authorId: user.id,
      };
      const createdFileMetadata: FileMetadata = await createFileMetadataApi(newFileDto);
      
      const newSuperObjectData: SuperObject = {
        fileId: createdFileMetadata.id, 
        serviceType: documentFileType.name,
        name: createdFileMetadata.name,
      };
      const createdSuperObject = await createSuperObject(newSuperObjectData);

      await linkFileToProjectApi(selectedProjectId, createdFileMetadata.id);

      if (createdSuperObject.id) {
        await updateFileSuperObjectId(createdFileMetadata.id, createdSuperObject.id);
        const newProjectFileEntry: ProjectFile = {
          id: createdFileMetadata.id,
          name: createdFileMetadata.name,
          type: createdFileMetadata.type,
          author: createdFileMetadata.authorId,
          date: createdFileMetadata.uploadDate,
          superObjectId: createdSuperObject.id, 
        };
        setFiles(prev => [...prev, newProjectFileEntry]);
        const roleToPass = currentSelectedProject.currentUserRole;
        const projectIdToPass = selectedProjectId;
        navigate(`/notes/${createdSuperObject.id}/edit`, {
          state: {
              currentUserRole: roleToPass,
              projectId: projectIdToPass
          }
      });
      } else {
        throw new Error("SuperObject был создан, но не вернул ID.");
      }
      handleCloseCreateFileModal();
    } catch (apiError: any) {
      console.error("Ошибка создания файла:", apiError);
      alert(`Ошибка создания файла: ${apiError.response?.data?.message || apiError.message || 'Неизвестная ошибка'}`);
    } finally {
      setIsSubmitting(false);
    }
  };
  
  const handleOpenFile = (superObjectIdToOpen: string) => {
    const projectForThisFile = projects.find(p => 
        p.projectFiles.some(f => f.superObjectId === superObjectIdToOpen)
    );
    
    const roleToPass = projectForThisFile ? projectForThisFile.currentUserRole : null;
    const projectIdToPass = projectForThisFile ? projectForThisFile.id : null;

    navigate(`/notes/${superObjectIdToOpen}/edit`, { 
        state: { 
            currentUserRole: roleToPass,
            projectId: projectIdToPass 
        } 
    });
  };

  const handleProjectNameUpdate = async (projectId: number, newName: string) => {
    const projectUpdatePayload: UpdateProjectPayload = { 
      name: newName,
    };
    
    try {
      const updatedProjectFromApi = await updateProjectApi(projectId, projectUpdatePayload);
      setProjects(prevProjects => 
        prevProjects.map(p => p.id === updatedProjectFromApi.id ? updatedProjectFromApi : p)
      );
    } catch (apiError: any) {
        console.error("Ошибка обновления проекта:", apiError);
        alert(`Ошибка обновления проекта: ${apiError.response?.data?.message || apiError.message || 'Неизвестная ошибка'}`);
    }
  };

  const handleUpdateFileName = async (fileIdToUpdate: number, superObjectId: string, newName: string) => {
    const fileToUpdate = files.find(f => f.id === fileIdToUpdate && f.superObjectId === superObjectId);
    if (!fileToUpdate) {
      console.error("Файл для обновления не найден в локальном стейте");
      alert("Файл не найден, обновление невозможно.");
      return;
    }
  
    setIsSubmitting(true);
    try {
      const payloadForBackendUpdateFile: FileCreateDto = { 
        name: newName, 
        typeId: fileToUpdate.type.id, 
        authorId: user!.id 
      };
      if (!user) { throw new Error("Пользователь не авторизован для обновления файла"); }
      (payloadForBackendUpdateFile as any).authorId = user.id;

      const updatedFileMetadata = await updateFileName(fileIdToUpdate, newName);
      await updateSuperObject(superObjectId, { name: newName });

      setFiles(prevFiles =>
        prevFiles.map(f => {
          if (f.id === fileIdToUpdate && f.superObjectId === superObjectId) {
            return { 
                ...f, 
                name: updatedFileMetadata.name, 
                date: updatedFileMetadata.uploadDate, 
                author: updatedFileMetadata.authorId,
             };
          }
          return f;
        })
      );
    } catch (apiError: any) {
      console.error("Ошибка обновления имени файла:", apiError);
      alert(`Ошибка обновления файла: ${apiError.response?.data?.message || apiError.message || 'Неизвестная ошибка'}`);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoadingProjects && projects.length === 0) {
    return <div style={{ padding: '20px' }}><p>Загрузка проектов...</p></div>;
  }

  return (
    <div className="project-dashboard-page">
      <Sidebar
        projects={projects.map(p => ({ id: p.id.toString(), name: p.name }))}
        selectedProjectId={selectedProjectId !== null ? selectedProjectId.toString() : null}
        onSelectProject={handleSelectProject}
        onCreateNewProject={handleOpenCreateProjectModal}
        isLoading={isLoadingProjects && projects.length === 0}
      />
      <MainArea
        selectedProject={currentProjectDetails} 
        files={files}
        onOpenFile={handleOpenFile} 
        onCreateNewFile={handleOpenCreateFileModal}
        isLoading={isLoadingFiles}
        onProjectNameUpdate={handleProjectNameUpdate}
        onUpdateFileName={handleUpdateFileName}
        onOpenAccessModal={handleOpenAccessModal}
      />
      <NameInputDialog
        isOpen={isCreateProjectModalOpen}
        onClose={handleCloseCreateProjectModal}
        onSubmit={handleSubmitNewProject}
        title="Создать новый проект"
        inputLabel="Имя проекта"
        isLoading={isSubmitting}
      />
      {selectedProjectId !== null && (
        <NameInputDialog
          isOpen={isCreateFileModalOpen}
          onClose={handleCloseCreateFileModal}
          onSubmit={handleSubmitNewFile}
          title="Создать новый файл"
          inputLabel="Имя файла"
          isLoading={isSubmitting}
        />
      )}
      {currentProjectDetails && user && (
        <ProjectAccessModal
            isOpen={isAccessModalOpen}
            onClose={handleCloseAccessModal}
            project={{
                id: currentProjectDetails.id,
                name: currentProjectDetails.name,
                participants: currentProjectDetails.participants || [],
                owner: currentProjectDetails.owner 
            }}
            onParticipantsUpdate={handleParticipantsUpdate}
            currentUserId={user.id}
        />
      )}
    </div>
  );
};

export default ProjectDashboardPage;
