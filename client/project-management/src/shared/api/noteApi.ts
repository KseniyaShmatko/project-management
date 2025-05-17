// src/shared/api/noteApi.ts
import apiClient from './client';
import { SuperObject, ContentBlock, EditorJsBlockData } from './models'; // Предполагаем, что типы в models.ts

// --- SuperObject API ---
export const getSuperObjectByFileId = async (fileId: number): Promise<SuperObject | null> => {
  try {
    const response = await apiClient.get<SuperObject>(`/super-objects/by-file/${fileId}`);
    return response.data;
  } catch (error: any) {
    if (error.response && error.response.status === 404) {
      return null; // Если не найдено, возвращаем null, а не кидаем ошибку
    }
    throw error; // Перебрасываем другие ошибки
  }
};

export const createSuperObject = async (superObjectData: SuperObject): Promise<SuperObject> => {
  const response = await apiClient.post<SuperObject>('/super-objects', superObjectData);
  return response.data;
};

export const updateSuperObject = async (id: string, superObjectData: Partial<SuperObject>): Promise<SuperObject> => {
  const response = await apiClient.put<SuperObject>(`/super-objects/${id}`, superObjectData);
  return response.data;
};

// Пока не добавляем deleteSuperObject, если не нужно немедленно

// --- ContentBlock API ---
// Важно: твой Swagger не описывает эндпоинт для получения всех блоков для SuperObject.
// Обычно, либо SuperObject приходит с вложенными блоками, либо есть отдельный эндпоинт
// типа GET /super-objects/{superObjectId}/content-blocks
// ИЛИ мы загружаем блоки по их ID, если знаем firstItem/nextItem.
//
// Для начала, предположим, что мы будем загружать блоки по цепочке nextItem,
// или что SuperObject будет содержать массив ID блоков.
//
// Самый простой вариант, если SuperObject *уже содержит* массив ID блоков,
// Или если нам нужно загрузить блоки по очереди, начиная с `firstItem`.

// Для индивидуальных операций с блоками:
export const getContentBlockById = async (blockId: string): Promise<ContentBlock> => {
  const response = await apiClient.get<ContentBlock>(`/content-blocks/${blockId}`);
  return response.data;
};

export const createContentBlock = async (blockData: ContentBlock): Promise<ContentBlock> => {
  // Backend ожидает ContentBlock целиком. Убедись, что prevItem/nextItem корректно установлены.
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

// Функция для загрузки всех блоков для SuperObject (нужно будет адаптировать)
// Эта функция предполагает, что мы знаем ID всех блоков или можем пройти по цепочке
// export const getAllContentBlocksForSuperObject = async (superObject: SuperObject): Promise<ContentBlock[]> => {
//   const blocks: ContentBlock[] = [];
//   let currentBlockId = superObject.firstItem;

//   while (currentBlockId) {
//     try {
//       const block = await getContentBlockById(currentBlockId);
//       blocks.push(block);
//       currentBlockId = block.nextItem;
//     } catch (error) {
//       console.error(`Failed to load block ${currentBlockId}`, error);
//       currentBlockId = null; // Прервать цикл при ошибке
//     }
//   }
//   return blocks;
// };

export const getAllContentBlocksForSuperObject = async (superObject: SuperObject): Promise<ContentBlock[]> => {
    const blocks: ContentBlock[] = [];
    if (!superObject.firstItem) {
      return blocks;
    }
    let currentBlockId: string | null | undefined = superObject.firstItem;
  
    while (currentBlockId) {
      try {
        // ВАЖНО: getContentBlockById должна быть экспортирована и импортирована здесь, если она в этом же файле
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
): Promise<SuperObject> => { // Предполагаем, что бэкенд вернет обновленный SuperObject
  const response = await apiClient.put<SuperObject>(
    `/super-objects/${superObjectId}/sync-blocks`,
    blocksPayload // Отправляем массив DTO
  );
  return response.data;
};