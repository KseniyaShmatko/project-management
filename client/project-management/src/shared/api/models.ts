// src/shared/api/models.ts (или entities/note/model.ts и entities/content-block/model.ts)

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
  