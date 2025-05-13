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
    id?: string; // ID будет присвоен бэкендом при создании
    objectType: string; // 'text', 'image', 'list', 'video', etc.
    nextItem?: string | null;
    prevItem?: string | null;
    data: BlockData; // Поле data теперь типизировано
    label?: string;
    // Для списков (по аналогии с твоим Swagger, где data была string)
    // data может быть объектом { items: string[] } для objectType: 'list'
    items?: string[]; // Если objectType === 'list'
    marker?: string;  // Если objectType === 'list'
    position?: string; // Например, 'left', 'center', 'right' для изображений
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
  