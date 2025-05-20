import apiClient from './client';
import { 
  SuperObject, ContentBlock, EditorJsBlockData, ApiUploadResponse, 
  Project, ProjectFile, CreateProjectPayload, UpdateProjectPayload,
  FileMetadata, FileCreateDto, FileType, ProjectParticipant, 
  LoginRequest, RegisterRequest, AuthResponse, UserProfile,
  ProjectUserActionPayload, ProjectRole
} from './models';

const AUTH_TOKEN_KEY = 'authToken';

export const setAuthToken = (token: string | null) => {
  if (token) {
    localStorage.setItem(AUTH_TOKEN_KEY, token);
    apiClient.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  } else {
    localStorage.removeItem(AUTH_TOKEN_KEY);
    delete apiClient.defaults.headers.common['Authorization'];
  }
};

export const getAuthToken = (): string | null => {
  return localStorage.getItem(AUTH_TOKEN_KEY);
};

const initialToken = getAuthToken();
if (initialToken) {
  setAuthToken(initialToken);
}

export const loginUserApi = async (loginData: LoginRequest): Promise<AuthResponse> => {
  const response = await apiClient.post<AuthResponse>('/users/login', loginData);
  if (response.data && response.data.token) {
    setAuthToken(response.data.token);
  }
  return response.data;
};

export const registerUserApi = async (registerData: RegisterRequest): Promise<UserProfile> => {
  const response = await apiClient.post<UserProfile>('/users/register', registerData);
  return response.data;
};

export const getCurrentUserApi = async (): Promise<UserProfile> => {
  const response = await apiClient.get<UserProfile>('/users/me');
  return response.data;
};

export const logoutUser = () => {
  setAuthToken(null);
};

export const createSuperObject = async (superObjectData: SuperObject): Promise<SuperObject> => {
  const response = await apiClient.post<SuperObject>('/super-objects', superObjectData);
  return response.data;
};

export const updateSuperObject = async (id: string, superObjectData: Partial<SuperObject>): Promise<SuperObject> => {
  const response = await apiClient.put<SuperObject>(`/super-objects/${id}`, superObjectData);
  return response.data;
};

export const getContentBlockById = async (blockId: string): Promise<ContentBlock> => {
  const response = await apiClient.get<ContentBlock>(`/content-blocks/${blockId}`);
  return response.data;
};

export const createContentBlock = async (blockData: ContentBlock): Promise<ContentBlock> => {
  const response = await apiClient.post<ContentBlock>('/content-blocks', blockData);
  return response.data;
};

export const updateContentBlock = async (id: string, blockData: Partial<ContentBlock>): Promise<ContentBlock> => {
  const response = await apiClient.put<ContentBlock>(`/content-blocks/${id}`, blockData);
  return response.data;
};

export const deleteContentBlock = async (id: string): Promise<void> => {
  await apiClient.delete(`/content-blocks/${id}`);
};

export const getAllContentBlocksForSuperObject = async (superObject: SuperObject): Promise<ContentBlock[]> => {
    const blocks: ContentBlock[] = [];
    if (!superObject.firstItem) {
      return blocks;
    }
    let currentBlockId: string | null | undefined = superObject.firstItem;
  
    while (currentBlockId) {
      try {
        const block = await getContentBlockById(currentBlockId);
        if (block) {
          blocks.push(block);
          currentBlockId = block.nextItem;
        } else {
          console.warn(`Block with id ${currentBlockId} not found, stopping chain.`);
          currentBlockId = null;
        }
      } catch (error: any) {
        if (error.response && error.response.status === 404) {
          console.warn(`Block with id ${currentBlockId} returned 404, stopping chain.`);
        } else {
          console.error(`Failed to load block ${currentBlockId}`, error);
        }
        currentBlockId = null;
      }
    }
    return blocks;
};

export const syncDocumentBlocksApi = async (
    superObjectId: string,
    blocksPayload: EditorJsBlockData[]
): Promise<SuperObject> => {
  const response = await apiClient.put<SuperObject>( `/super-objects/${superObjectId}/sync-blocks`,  blocksPayload);

  return response.data;
};

export const uploadImageApi = async (file: File): Promise<ApiUploadResponse> => {
  const formData = new FormData();
  formData.append('image', file);

  try {
    const response = await apiClient.post<ApiUploadResponse>(
      '/files-storage/upload/image',
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    return response.data;
  } catch (error: any) {
    console.error('API Error in uploadImageApi:', error.response?.data || error.message);
    return {
      success: 0,
      file: { url: '', name: file.name },
      message: error.response?.data?.message || error.message || 'Ошибка загрузки файла на сервер',
    };
  }
};

export const getAllProjectsApi = async (): Promise<Project[]> => {
  try {
    const response = await apiClient.get<Project[]>('/projects'); 
    return response.data;
  } catch (error) {
    console.error("Ошибка при получении списка проектов:", error);
    if ((error as any).response?.status === 401) {
        logoutUser();
    }
    throw error;
  }
};

export const createProjectApi = async (projectData: CreateProjectPayload): Promise<Project> => {
  const response = await apiClient.post<Project>('/projects', projectData);
  return response.data;
};

export const getProjectByIdApi = async (projectId: number): Promise<Project> => {
  const response = await apiClient.get<Project>(`/projects/${projectId}`);
  return response.data;
};

export const createFileMetadataApi = async (fileDto: FileCreateDto): Promise<FileMetadata> => {
  const response = await apiClient.post<FileMetadata>('/files', fileDto);
  return response.data;
};

export const updateFileApi = async (fileId: number, fileUpdateDto: Partial<FileCreateDto>): Promise<FileMetadata> => {
  const response = await apiClient.put<FileMetadata>(`/files/${fileId}`, fileUpdateDto);
  return response.data;
};

export const linkFileToProjectApi = async (projectId: number, fileId: number): Promise<{project_id: number, file_id: number}> => { 
  const response = await apiClient.post(`/projects/${projectId}/files/link?file_id=${fileId}`);
  return response.data;
};

export const getAllFileTypesApi = async (): Promise<FileType[]> => {
  const response = await apiClient.get<FileType[]>('/file-types');
  return response.data;
};

export const getFilesForProjectApi = async (projectId: number): Promise<ProjectFile[]> => {
  const response = await apiClient.get<ProjectFile[]>(`/projects/${projectId}/files`); 
  return response.data;
};

export const getSuperObjectByFileId = async (fileId: number): Promise<SuperObject | null> => {
  try {
    const response = await apiClient.get<SuperObject>(`/super-objects/by-file/${fileId}`);
    return response.data;
  } catch (error: any) {
    if (error.response && error.response.status === 404) {
      return null; 
    }
    console.error(`Ошибка загрузки SuperObject по FileID ${fileId}:`, error);
    throw error;
  }
};

export const updateProjectApi = async (projectId: number, projectUpdateData: UpdateProjectPayload): Promise<Project> => {
  const response = await apiClient.put<Project>(`/projects/${projectId}`, projectUpdateData);
  return response.data;
};

export const getSuperObjectByMongoId = async (mongoId: string): Promise<SuperObject | null> => {
  try {
    const response = await apiClient.get<SuperObject>(`/super-objects/${mongoId}`);
    return response.data;
  } catch (error: any) {
    if (error.response && error.response.status === 404) {
      return null;
    }
    console.error(`Ошибка загрузки SuperObject по MongoID ${mongoId}:`, error);
    throw error;
  }
};

export const updateFileSuperObjectId = async (fileId: number, superObjectId: string): Promise<FileMetadata> => {
  const response = await apiClient.patch<FileMetadata>(
    `/files/${fileId}/super-object`, 
    { superObjectId }
  );
  return response.data;
};

export const updateFileName = async (fileId: number, name: string): Promise<FileMetadata> => {
  const response = await apiClient.patch<FileMetadata>(`/files/${fileId}/name`, { name });
  return response.data;
};

export const searchUsersByLoginApi = async (loginQuery: string): Promise<UserProfile[]> => {
  const response = await apiClient.get<UserProfile[]>(`/users/search?login=${encodeURIComponent(loginQuery)}`);
  return response.data;
};

export const getProjectParticipantsApi = async (projectId: number): Promise<ProjectParticipant[]> => {
  const response = await apiClient.get<ProjectParticipant[]>(`/projects-users/project/${projectId}/users`);
  return response.data;
};

export const linkUserToProjectApi = async (payload: ProjectUserActionPayload): Promise<ProjectParticipant> => {
    const response = await apiClient.post<ProjectParticipant>('/projects-users', payload);
    return response.data;
};

export const updateUserProjectRoleApi = async (projectId: number, userId: number, role: ProjectRole): Promise<ProjectParticipant> => {
    const response = await apiClient.put<ProjectParticipant>(
        `/projects-users/project/${projectId}/user/${userId}`,
        { role }
    );
    return response.data;
};

export const removeUserFromProjectApi = async (projectId: number, userId: number): Promise<void> => {
  await apiClient.delete(`/projects-users/project/${projectId}/user/${userId}`);
};
