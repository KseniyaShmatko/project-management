export interface Template {
  image?: string;
  type?: string;
  color?: string;
}
  
export interface Decoration {
  marginTop?: number;
  marginRight?: number;
  marginLeft?: number;
  marginBottom?: number;
}

export interface TextBlockData {
  text: string;
}

export interface ImageBlockData {
  url: string;
  caption?: string;
}

export type BlockData = string | ImageBlockData | { [key: string]: any };

export interface ContentBlock {
  id?: string;
  objectType: string;
  nextItem?: string | null;
  prevItem?: string | null;
  data: { [key: string]: any };
}

export interface EditorJsBlockData {
  id?: string | null;
  type: string;
  data: { [key: string]: any };
}

export interface SuperObject {
  id?: string; 
  fileId: number;
  serviceType: string; 
  lastChangeDate?: string;
  name?: string;
  template?: Template;
  decoration?: Decoration;
  firstItem?: string | null; 
  lastItem?: string | null;
  checkSum?: number;
  stylesMapId?: string;
}

export interface ApiUploadResponse { 
  success: 1 | 0;
  file: {
    url: string;
    name?: string;
    size?: number;
    [key: string]: any;
  };
  message?: string;
}

export interface DisplayFile { 
  id: number;
  name: string;
  type: FileType;
  author: number;
  date: string;
  superObjectId: string;
}

export interface LoginRequest {
  login: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  surname: string;
  login: string;
  password: string;
  photo?: string | null;
}

export interface AuthResponse {
  token: string;
  id: number;
  login: string;
  name: string;
  surname: string;
  photo?: string | null;
}

export interface UserProfile {
  id: number;
  name: string;
  surname: string;
  login: string;
  photo?: string | null;
}

export interface ProjectOwner { 
  id: number;
  name: string;
  surname: string;
  login: string;
  photo?: string | null;
}

export interface ProjectFile {
  id: number; 
  name: string;
  type: FileType;
  author: number;
  date: string;
  superObjectId: string | null;
}

export interface FileType {
  id: number;
  name: string;
}

export interface FileMetadata {
  id: number;
  name: string;
  type: FileType;
  authorId: number; 
  uploadDate: string;
  superObjectId?: string | null;
  filePath?: string | null;
}

export interface FileCreateDto {
  name: string;
  typeId: number;
  authorId: number; 
  superObjectId?: string | null; 
}

export interface CreateProjectPayload {
  name: string;
  description?: string;
}

export interface UpdateProjectPayload {
  name: string;
  description?: string;
}

export enum ProjectRole {
  OWNER = "OWNER",
  EDITOR = "EDITOR",
  VIEWER = "VIEWER",
}

export interface ProjectParticipant {
  id: number;
  projectId: number;
  userId: number;
  login: string;
  name?: string | null;
  surname?: string | null;
  photo?: string | null;
  role: ProjectRole;
}

export interface ProjectUserActionPayload {
  projectId: number;
  userId: number;
  role: ProjectRole;
}

export interface Project {
  id: number;
  name: string;
  description?: string | null;
  owner: ProjectOwner | null;
  projectFiles: ProjectFile[];
  participants: ProjectParticipant[];
  currentUserRole: ProjectRole | null;
}
