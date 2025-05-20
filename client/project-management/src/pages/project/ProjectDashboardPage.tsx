// src/pages/project/ProjectDashboardPage.tsx
import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';

import Sidebar from '../../features/sidebar/Sidebar'; // Убедитесь, что пути верны
import MainArea from '../../features/main-area/MainArea'; // Убедитесь, что пути верны
import NameInputDialog from '../../shared/ui/modals/NameInputDialog'; // Убедитесь, что пути верны
import ProjectAccessModal from '../../shared/ui/modals/ProjectAccessModal';

import {
  // Обновленные типы, которые приходят с бэкенда или используются для запросов
  Project,        // -> ProjectResponseDto (с owner: ProjectOwner, projectFiles: ProjectFile[])
  ProjectFile,    // -> ProjectFileResponseDto
  FileType,       // -> FileType (сущность)
  FileMetadata,   // -> File (сущность, возвращаемая FileController)
  SuperObject,
  // Типы для создания/обновления
  CreateProjectPayload, // { name, description? }
  UpdateProjectPayload, // { name, description? }
  FileCreateDto,        // { name, typeId } - authorId теперь не нужен
  ProjectParticipant
} from '../../shared/api/models';

import {
  // API аутентификации (не используется напрямую здесь, но UserProfile из useAuth)
  // loginUserApi, registerUserApi, getCurrentUserApi,
  
  // API проектов
  getAllProjectsApi,     // Переименовал для единообразия с Client-Side API
  createProjectApi,
  updateProjectApi,
  // getProjectByIdApi, // Если понадобится

  // API файлов
  getFilesForProjectApi, // Возвращает ProjectFile[]
  createFileMetadataApi, // Принимает FileCreateDto, возвращает FileMetadata
  updateFileName,         // Принимает fileId и Partial<FileCreateDto>, возвращает FileMetadata
  linkFileToProjectApi,
  getAllFileTypesApi, 
  updateFileSuperObjectId,   // Возвращает FileType[]

  // API SuperObject
  createSuperObject,
  updateSuperObject,
  // getSuperObjectByFileId, getSuperObjectByMongoId
} from '../../shared/api/noteApi'; 

import { useAuth } from '../../shared/context/AuthContext'; // Хук для получения данных пользователя

import './ProjectDashboardPage.scss';

const ProjectDashboardPage: React.FC = () => {
  const [projects, setProjects] = useState<Project[]>([]);
  const [selectedProjectId, setSelectedProjectId] = useState<number | null>(null);
  const [files, setFiles] = useState<ProjectFile[]>([]); // Теперь это ProjectFile[]
  const [fileTypes, setFileTypes] = useState<FileType[]>([]);

  const [isLoadingProjects, setIsLoadingProjects] = useState<boolean>(true);
  const [isLoadingFiles, setIsLoadingFiles] = useState<boolean>(false);
  const [isCreateProjectModalOpen, setIsCreateProjectModalOpen] = useState(false);
  const [isCreateFileModalOpen, setIsCreateFileModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isAccessModalOpen, setIsAccessModalOpen] = useState(false);

  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth(); // Получаем текущего пользователя

  // Загрузка проектов и типов файлов
  useEffect(() => {
    setIsLoadingProjects(true);
    Promise.all([getAllProjectsApi(), getAllFileTypesApi()]) // Используем getAllProjectsApi
      .then(([fetchedProjects, fetchedFileTypes]) => {
        setProjects(fetchedProjects);
        setFileTypes(fetchedFileTypes);
      })
      .catch(error => {
        console.error("Ошибка загрузки начальных данных:", error);
        // TODO: Обработать ошибки, например, показать уведомление пользователю
      })
      .finally(() => setIsLoadingProjects(false));
  }, []);

  // Загрузка файлов для выбранного проекта
  useEffect(() => {
    if (selectedProjectId !== null) {
      setIsLoadingFiles(true);
      setFiles([]); // Сбрасываем предыдущие файлы
      
      getFilesForProjectApi(selectedProjectId)
        .then(fetchedProjectFiles => {
          // Проверяем структуру каждого файла
          fetchedProjectFiles.forEach((file, index) => {
            console.log(`Файл ${index}:`, file);
            console.log(`   - type:`, file.type);
            console.log(`   - superObjectId:`, file.superObjectId);
          });
          
          // Проверяем файлы после фильтрации
          const filteredFiles = fetchedProjectFiles.filter(f => f.superObjectId != null);
          console.log('Отфильтрованные файлы:', filteredFiles);
          
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
    // Обновляем участника в стейте projects
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

  // Создание нового проекта
  const handleSubmitNewProject = async (projectName: string) => {
    if (!user) { // Проверка, что пользователь авторизован (хотя ProtectedRoute уже должен это делать)
      alert("Пользователь не авторизован!");
      return;
    }
    setIsSubmitting(true);
    try {
      // authorId и date теперь не нужны, бэкенд их сам обработает
      const newProjectPayload: CreateProjectPayload = { name: projectName /*, description: "..." */ };
      const newProjectFromApi = await createProjectApi(newProjectPayload);
      setProjects(prev => [...prev, newProjectFromApi]);
      setSelectedProjectId(newProjectFromApi.id); // Выбираем новый проект
      handleCloseCreateProjectModal();
    } catch (apiError: any) {
      console.error("Ошибка создания проекта:", apiError);
      alert(`Ошибка создания проекта: ${apiError.response?.data?.message || apiError.message || 'Неизвестная ошибка'}`);
    } finally {
      setIsSubmitting(false);
    }
  };

  // Создание нового файла
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
      console.log('fileTypes', fileTypes)
      // Находим тип файла "document" или "note"
      const documentFileType = fileTypes.find(ft => 
        ft.name.toLowerCase() === 'note'
      );
      if (!documentFileType) {
        alert("Системный тип файла 'document'/'note' не найден. Обратитесь к администратору.");
        setIsSubmitting(false); return;
      }

      // DTO для создания File. authorId теперь не передается, т.к. берется из токена на бэке
      const newFileDto: FileCreateDto = { 
        name: fileName, 
        typeId: documentFileType.id,
        authorId: user.id,
      };
      const createdFileMetadata: FileMetadata = await createFileMetadataApi(newFileDto);
      
      // Создаем SuperObject
      const newSuperObjectData: SuperObject = {
        fileId: createdFileMetadata.id, 
        serviceType: documentFileType.name, // Или используйте имя из createdFileMetadata.type.name
        name: createdFileMetadata.name,
      };
      const createdSuperObject = await createSuperObject(newSuperObjectData);

      // Связываем файл с проектом
      await linkFileToProjectApi(selectedProjectId, createdFileMetadata.id);

      if (createdSuperObject.id) {
        await updateFileSuperObjectId(createdFileMetadata.id, createdSuperObject.id);
        // Создаем объект для отображения в списке файлов
        // Используем данные из createdFileMetadata и createdSuperObject
        const newProjectFileEntry: ProjectFile = {
          id: createdFileMetadata.id,
          name: createdFileMetadata.name,
          type: createdFileMetadata.type, // createFileMetadataApi возвращает FileMetadata с полным объектом FileType
          author: createdFileMetadata.authorId, // FileMetadata содержит authorId
          date: createdFileMetadata.uploadDate,    // FileMetadata содержит uploadDate (строка)
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
    // Находим текущий проект, чтобы получить currentUserRole
    const projectForThisFile = projects.find(p => 
        p.projectFiles.some(f => f.superObjectId === superObjectIdToOpen)
    );
    
    const roleToPass = projectForThisFile ? projectForThisFile.currentUserRole : null;
    const projectIdToPass = projectForThisFile ? projectForThisFile.id : null; // Также передадим projectId

    console.log(`Navigating to note ${superObjectIdToOpen}, passing role: ${roleToPass}, projectId: ${projectIdToPass}`);

    navigate(`/notes/${superObjectIdToOpen}/edit`, { 
        state: { 
            currentUserRole: roleToPass,
            projectId: projectIdToPass // Передаем и ID проекта, может пригодиться
        } 
    });
  };

  // Обновление имени проекта
  const handleProjectNameUpdate = async (projectId: number, newName: string) => {
    // const projectToUpdate = projects.find(p => p.id === projectId);
    // if (!projectToUpdate) { // Эта проверка не очень надежна, если projects еще не обновлен
    //   console.error("Проект для обновления не найден в локальном стейте:", projectId);
    //   // Можно попробовать обновить имя оптимистично или перезапросить проект
    //   // Для простоты пока оставим так, но если будут проблемы, эту логику надо улучшить
    // }

    // Payload для обновления. Бэкенд ProjectController.updateProject ожидает @RequestBody project: Project
    // Но сервис ProjectService.updateProject использует только name и description из этого.
    const projectUpdatePayload: UpdateProjectPayload = { 
      name: newName,
      // description: projectToUpdate?.description // Если хотите сохранить description или дать возможность его менять
    };
    
    try {
      const updatedProjectFromApi = await updateProjectApi(projectId, projectUpdatePayload);
      setProjects(prevProjects => 
        prevProjects.map(p => p.id === updatedProjectFromApi.id ? updatedProjectFromApi : p)
      );
    } catch (apiError: any) {
        console.error("Ошибка обновления проекта:", apiError);
        alert(`Ошибка обновления проекта: ${apiError.response?.data?.message || apiError.message || 'Неизвестная ошибка'}`);
        // Можно реализовать откат оптимистичного обновления, если оно было
    }
  };

  // Обновление имени файла
  const handleUpdateFileName = async (fileIdToUpdate: number, superObjectId: string, newName: string) => {
    const fileToUpdate = files.find(f => f.id === fileIdToUpdate && f.superObjectId === superObjectId);
    if (!fileToUpdate) {
      console.error("Файл для обновления не найден в локальном стейте");
      alert("Файл не найден, обновление невозможно.");
      return;
    }
  
    setIsSubmitting(true); // Можно использовать отдельный индикатор загрузки для карточки файла
    try {
      // Шаг 1: Обновляем метаданные файла (File)
      // Бэкенд FileController.updateFile ожидает @RequestBody updateDto: FileDto
      // FileDto: { name: String, typeId: Long, authorId: Long }
      // Нам нужно передать все эти поля, т.к. бэк ожидает полный FileDto.
      const updateFilePayload: FileCreateDto = { // Используем FileCreateDto как тип, но это FileDto для бэка
          name: newName,
          typeId: fileToUpdate.type.id, // Берем текущий typeId
          authorId: fileToUpdate.author, // authorId на бэке теперь берется из токена, передавать не нужно,
                                         // если FileService.updateFile адаптирован.
                                         // Если FileService.updateFile все еще ТРЕБУЕТ authorId в DTO,
                                         // то нужно его взять из fileToUpdate.author
      };
      // Уточнение: Если бэкенд FileService.updateFile не меняет автора, то authorId 
      // в FileDto используется для проверки прав или просто является частью DTO.
      // Если автор НЕ должен меняться и бэк его не требует для обновления имени,
      // то на бэке нужен другой DTO для PUT /files/{id} (например, UpdateFileNameDto).
      // Пока предполагаем, что FileDto используется, и authorId (если он есть в FileDto бэка) должен быть корректным.
      // Для простоты, если ваш FileDto на бэке имеет authorId, то передадим его:
      // const updateFilePayloadWithAuthor: FileCreateDto & { authorId: number } = {
      //    name: newName,
      //    typeId: fileToUpdate.type.id,
      //    authorId: fileToUpdate.author, // Предполагая, что `fileToUpdate.author` это и есть authorId
      // };
      // Лучше если на бэке updateFile не требует authorId, если он не меняется.
      // Сейчас FileDto на бэке ТРЕБУЕТ authorId.
      const payloadForBackendUpdateFile: FileCreateDto = { 
        name: newName, 
        typeId: fileToUpdate.type.id, 
        authorId: user!.id // Берем ID текущего авторизованного пользователя!
                           // Это более правильно, чем fileToUpdate.author, если автор может меняться
                           // или если это проверка прав.
                           // Однако, если автор файла не должен меняться, а это просто поле в DTO,
                           // то fileToUpdate.author (который должен быть authorId)
                           // Если FileService.updateFile не изменяет автора, а просто принимает его в DTO,
                           // то fileToUpdate.author более корректно.
                           // Для безопасности, если бэк ожидает authorId в FileDto, лучше передавать
                           // ID текущего пользователя, чтобы гарантировать, что он имеет права на это действие.
                           // Но еще лучше, если бэк сам проверяет права по токену, а в DTO только name.
                           // ИСХОДЯ ИЗ ВАШЕГО FileDto НА БЭКЕ, ОН ОЖИДАЕТ authorId.
                           // Передадим ID текущего юзера, если он есть.
      };
      if (!user) { throw new Error("Пользователь не авторизован для обновления файла"); }
      (payloadForBackendUpdateFile as any).authorId = user.id; // Приводим к типу с authorId, если FileCreateDto его не имеет


      const updatedFileMetadata = await updateFileName(fileIdToUpdate, newName);
  
      console.log('updatedFileMetadata', updatedFileMetadata)
      // Шаг 2: Обновляем имя в SuperObject (если нужно, и если имя SuperObject связано с именем File)
      // Часто имя SuperObject - это и есть имя файла.
      await updateSuperObject(superObjectId, { name: newName });
  
      // Шаг 3: Обновляем состояние на клиенте
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
      // throw apiError; // Если обработка ошибки идет в вызывающем компоненте (FileCard)
    } finally {
      setIsSubmitting(false);
    }
  };

  const selectedProject = projects.find(p => p.id === selectedProjectId);

  if (isLoadingProjects && projects.length === 0) { // Показываем загрузку только если проектов еще нет
    return <div style={{ padding: '20px' }}><p>Загрузка проектов...</p></div>;
  }

  return (
    <div className="project-dashboard-page"> {/* Общий layout для страницы */}
      <Sidebar
        projects={projects.map(p => ({ id: p.id.toString(), name: p.name }))}
        selectedProjectId={selectedProjectId !== null ? selectedProjectId.toString() : null}
        onSelectProject={handleSelectProject}
        onCreateNewProject={handleOpenCreateProjectModal}
        isLoading={isLoadingProjects && projects.length === 0} // Показываем лоадер в сайдбаре, если еще нет проектов
      />
      <MainArea
        selectedProject={currentProjectDetails} 
        files={files} // Передаем ProjectFile[]
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
      {selectedProjectId !== null && ( // Показываем модалку создания файла только если выбран проект
        <NameInputDialog
          isOpen={isCreateFileModalOpen}
          onClose={handleCloseCreateFileModal}
          onSubmit={handleSubmitNewFile}
          title="Создать новый файл" // Заголовок можно сделать динамическим, если тип файла выбирается
          inputLabel="Имя файла"
          isLoading={isSubmitting}
        />
      )}
      {currentProjectDetails && user && ( // Показываем модалку только если есть проект и пользователь
        <ProjectAccessModal
            isOpen={isAccessModalOpen}
            onClose={handleCloseAccessModal}
            project={{ // Собираем нужные данные для модалки
                id: currentProjectDetails.id,
                name: currentProjectDetails.name,
                participants: currentProjectDetails.participants || [], // Убедимся, что participants есть
                owner: currentProjectDetails.owner 
            }}
            onParticipantsUpdate={handleParticipantsUpdate}
            currentUserId={user.id} // ID текущего авторизованного пользователя
        />
      )}
    </div>
  );
};

export default ProjectDashboardPage;
