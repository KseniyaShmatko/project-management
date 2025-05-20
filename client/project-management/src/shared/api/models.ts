// src/shared/api/models.ts 

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

// Типы для данных внутри ContentBlock (нужно будет расширять по мере добавления типов блоков)
export interface TextBlockData {
  text: string;
}

export interface ImageBlockData {
  url: string;
  caption?: string;
}

// Добавь сюда другие типы данных для разных objectType в ContentBlock
export type BlockData = string | ImageBlockData | { [key: string]: any }; // Общий тип

export interface ContentBlock {
  id?: string;
  objectType: string; // Тип от Editor.js, сохраненный на бэке
  nextItem?: string | null;
  prevItem?: string | null;
  data: { [key: string]: any }; // data от Editor.js блока, сохраненное на бэке
}

// DTO для отправки данных блока на бэкенд (в метод syncDocumentBlocks)
export interface EditorJsBlockData { // Добавил Dto в конце для ясности, что это для передачи
  id?: string | null; // ID, который мог сгенерировать Editor.js (может быть null для новых блоков)
  type: string;        // Тип блока из Editor.js (e.g., "paragraph", "header")
  data: { [key: string]: any }; // Объект данных блока из Editor.js
  // tunes?: { [key: string]: any }; // Если используешь "tunes" (настройки блока)
}

export interface SuperObject {
  id?: string; // ID будет присвоен бэкендом при создании
  fileId: number; // Связь с файлом в PostgreSQL
  serviceType: string; // Например, 'document', 'diagram'
  lastChangeDate?: string;
  name?: string; // Название самого суперобъекта (может дублировать название файла)
  template?: Template;
  decoration?: Decoration;
  firstItem?: string | null; // ID первого ContentBlock
  lastItem?: string | null;  // ID последнего ContentBlock
  checkSum?: number;
  stylesMapId?: string;
  // Добавим поле для хранения самих блоков контента, загруженных с бэкенда
  // На бэкенде они могут храниться отдельно, но на клиенте удобнее иметь их внутри.
  // Либо мы будем загружать их по мере необходимости.
  // Для начала, можно предположить, что SuperObject *может* приходить с уже вложенными блоками,
  // или мы будем загружать их отдельно.
  // В твоей схеме Swagger нет contentBlocks внутри SuperObject, они получаются отдельно.
  // Значит, мы будем хранить их отдельно в состоянии.
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

// export interface Project { // Соответствует вашей сущности Project на бэке
//   id: number; // На бэке Long, на фронте можно number
//   author: number; // ID автора
//   date: string; // ISO строка даты
//   name: string;
// }

// export interface FileType { // Соответствует FileType на бэке
//   id: number;
//   name: string;
// }

// export interface FileMetadata { // Соответствует File на бэке
//   id: number;
//   name: string;
//   type: FileType; // Или просто typeId: number, если FileType не нужен полностью
//   author: number;
//   date: string;
// }

// DTO для создания файла (FileDto на бэке)
// export interface FileCreateDto {
//   name: string;
//   typeId: number; // ID типа файла (например, 'document', 'diagram' и т.д. должны иметь свои ID)
//   author: number; // ID текущего пользователя
//   // date будет установлена на бэке
// }

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

// Ответ от /users/login и /users/me (UserInfo + Token для логина)
export interface AuthResponse {
  token: string;
  id: number;
  login: string;
  name: string;
  surname: string;
  photo?: string | null;
}

// Данные пользователя (для профиля в Sidebar, можно использовать часть AuthResponse)
export interface UserProfile {
  id: number;
  name: string;
  surname: string;
  login: string;
  photo?: string | null;
}

// Пользователь, как часть ProjectResponseDto (соответствует UserResponseDto на бэке)
export interface ProjectOwner { 
  id: number;
  name: string;
  surname: string;
  login: string;
  photo?: string | null;
}

// Проект (соответствует ProjectResponseDto на бэке)
// export interface Project {
//   id: number;
//   name: string;
//   description?: string | null; // У вас на бэке description: String?
//   owner: ProjectOwner | null; // Владелец проекта
//   projectFiles: ProjectFile[]; // Список файлов проекта, пока оставим как ProjectFile[]
//                                // нужно будет создать ProjectFile интерфейс соответствующий ProjectFileResponseDto
// }

// Файл проекта (соответствует ProjectFileResponseDto на бэке)
export interface ProjectFile {
  id: number; // Это ID самого файла (File.id)
  name: string;
  type: FileType; // FileType у вас уже есть
  author: number; // На бэке это authorId, но в DTO было author
                  // Уточните, какое имя приходит с бэка в ProjectFileResponseDto.author
                  // Я оставлю author, как было в вашем ProjectFileResponseDto
  date: string; // Форматированная дата
  superObjectId: string | null; // MongoDB ID
}

export interface FileType { // Соответствует FileType на бэке
  id: number;
  name: string;
}

// Метаданные файла (используется для создания, соответствует File на бэке)
export interface FileMetadata {
  id: number;
  name: string;
  type: FileType;
  authorId: number; // На бэке authorId, на фронте был author, приводим к authorId
  uploadDate: string; // На бэке LocalDateTime, здесь будет строка (ISO или форматированная)
  superObjectId?: string | null;
  filePath?: string | null;
}

// DTO для создания файла File (соответствует FileCreateDto на бэке)
// Важно: на бэке FileCreateDto ожидает typeId, authorId.
// На фронте у вас был FileCreateDto с author, приводим к authorId.
export interface FileCreateDto {
  name: string;
  typeId: number;
  authorId: number; // ID текущего пользователя
  // uploadDate и filePath устанавливаются на бэке или при загрузке файла
  superObjectId?: string | null; // Это поле может быть нужно для связи на бэке сразу
}


// DisplayFile у вас был как ProjectFileResponseDto - переименуем для ясности
// и убедимся, что поля соответствуют ProjectFileResponseDto с бэка.
// Фактически, это будет ProjectFile.
// Я закомментирую DisplayFile, так как ProjectFile теперь выполняет его роль.
/*
export interface DisplayFile { 
  id: number;
  name: string;
  type: FileType;
  author: number; // или authorId, в зависимости от бэка
  date: string; // или uploadDate
  superObjectId: string;
}
*/

export interface CreateProjectPayload {
  name: string;
  description?: string;
}

export interface UpdateProjectPayload { // Должен содержать поля, которые бэк ожидает в update: Project
  name: string;
  description?: string;
  // owner и projectFiles обычно не обновляются через такой PUT на сам проект
}

export enum ProjectRole {
  OWNER = "OWNER",
  EDITOR = "EDITOR",
  VIEWER = "VIEWER",
}

export interface ProjectParticipant {
  id: number; // ID самой связи ProjectUser
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