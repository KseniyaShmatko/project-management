// src/widgets/note-editor/ui/NoteEditorWidget.tsx
import React, { useState, useEffect, useCallback, useRef } from 'react';
import EditorJsWrapper from './EditorJsWrapper';
import  EditorJS, { OutputData, BlockToolData, API as EditorJSAPI } from '@editorjs/editorjs';
import { debounce } from 'lodash-es'; // Убедитесь, что lodash-es установлен

import { SuperObject, ContentBlock, EditorJsBlockData } from '../../../shared/api/models';
import {
  getSuperObjectByFileId,
  createSuperObject,
  syncDocumentBlocksApi,
  getAllContentBlocksForSuperObject,
} from '../../../shared/api/noteApi';
import './NoteEditorWidget.scss';

interface NoteEditorWidgetProps {
  noteId?: string; // fileId передается как строка
}

// Конвертеры (оставляем как есть, если они работали корректно с типами)
const convertContentBlocksToEditorJsData = (contentBlocksFromDb: ContentBlock[]): OutputData => {
  if (!contentBlocksFromDb || contentBlocksFromDb.length === 0) {
    return { time: Date.now(), blocks: [{ type: 'paragraph', data: { text: '' } }], version: "2.30.0" }; // Начинаем с пустого параграфа
  }
  return {
    time: Date.now(),
    blocks: contentBlocksFromDb.map(dbBlock => {
      let blockTypeForEditor = dbBlock.objectType;
      if (blockTypeForEditor === 'unknown_tool_error' || !blockTypeForEditor) {
        blockTypeForEditor = 'paragraph';
      }
      let blockDataForEditor = dbBlock.data || {};
      if (blockTypeForEditor === 'paragraph' && typeof blockDataForEditor.text !== 'string') {
        blockDataForEditor = { text: '' };
      }
      return {
        id: dbBlock.id, // Используем ID из БД
        type: blockTypeForEditor,
        data: blockDataForEditor,
      };
    }),
    version: "2.30.0", // Укажите вашу актуальную стабильную версию Editor.js
  };
};

const convertEditorJsBlocksToPayload = (editorJsBlocks: BlockToolData[]): EditorJsBlockData[] => {
  return editorJsBlocks.map((ejBlock) => {
    const blockType = (ejBlock as any).type || (ejBlock as any).tool;
    if (!blockType) {
      console.error(`[CONVERT PAYLOAD] Блок ОТ Editor.js НЕ ИМЕЕТ 'type' или 'tool'! Блок:`, ejBlock);
      return {
        id: ejBlock.id, // Может быть null или undefined, если блок новый и ЕЩЕ не сохранен на сервере
        type: 'unknown_type_error_client',
        data: ejBlock.data || {},
      };
    }
    return {
      id: ejBlock.id, // ID блока (может быть сгенерирован клиентом для новых, или серверный для существующих)
      type: blockType as string,
      data: ejBlock.data,
    };
  });
};


export const NoteEditorWidget: React.FC<NoteEditorWidgetProps> = ({ noteId }) => {
  const [currentSuperObject, setCurrentSuperObject] = useState<SuperObject | null>(null);
  // editorInitialData теперь будет хранить данные, с которыми БЫЛ ИНИЦИАЛИЗИРОВАН редактор
  const [editorInitialDataUsed, setEditorInitialDataUsed] = useState<OutputData | undefined>(undefined);
  
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  
  const latestEditorDataRef = useRef<OutputData | null>(null);
  const currentSuperObjectRefForDebounce = useRef<SuperObject | null>(null);
  const loadingFileIdRef = useRef<number | null>(null);

  // Ref на инстанс EditorJS, чтобы управлять им из родителя, если нужно (например, для render)
  const editorJsInstanceApiRef = useRef<EditorJSAPI | null>(null); // Или EditorJS, если нужен весь инстанс
  const editorJsInstanceRef = useRef<EditorJS | null>(null);


  useEffect(() => {
    currentSuperObjectRefForDebounce.current = currentSuperObject;
  }, [currentSuperObject]);


  // Эффект для загрузки данных или создания нового SuperObject
  useEffect(() => {
    if (!noteId) {
      // ... (логика сброса состояния, если нет noteId) ...
      setIsLoading(false); setError('ID заметки не предоставлен.'); setEditorInitialDataUsed(undefined); setCurrentSuperObject(null); loadingFileIdRef.current = null; editorJsInstanceApiRef.current = null;
      return;
    }
    const fileId = parseInt(noteId, 10);
    if (isNaN(fileId)) {
      // ... (логика сброса состояния, если некорректный noteId) ...
      setIsLoading(false); setError('Некорректный ID заметки.'); setEditorInitialDataUsed(undefined); setCurrentSuperObject(null); loadingFileIdRef.current = null; editorJsInstanceApiRef.current = null;
      return;
    }

    console.log(`[LOAD EFFECT ROOT] noteId: ${noteId}, current loadingFileId: ${loadingFileIdRef.current}, isLoading: ${isLoading}`);

    // Если уже идет загрузка для этого fileId, выходим, чтобы избежать гонки.
    // Это простая проверка, для более сложной логики отмены предыдущего запроса можно использовать AbortController.
    if (loadingFileIdRef.current === fileId && isLoading) {
        console.log(`[LOAD EFFECT] Загрузка для fileId ${fileId} уже в процессе. Пропускаем.`);
        return;
    }

    if (!currentSuperObject || currentSuperObject.fileId !== fileId) {
        console.log(`[LOAD EFFECT] Смена noteId (${currentSuperObject?.fileId} -> ${fileId}) или первая загрузка.`);
        loadingFileIdRef.current = fileId;
        setIsLoading(true);
        setError(null);
        setCurrentSuperObject(null);
        setEditorInitialDataUsed(undefined); // Сброс initialData, так как EditorJsWrapper будет перемонтирован по key
        latestEditorDataRef.current = null;
        editorJsInstanceApiRef.current = null;
    } else if (isLoading && loadingFileIdRef.current === fileId) {
        // Загрузка для этого fileId уже идет, выходим
        console.log(`[LOAD EFFECT] Загрузка для fileId ${fileId} уже в процессе (isLoading=true). Пропускаем.`);
        return;
    } else if (!isLoading && currentSuperObject && currentSuperObject.fileId === fileId) {
        // Данные для этого fileId уже загружены, и мы не в состоянии isLoading.
        // Это может быть повторный вызов эффекта (например, от StrictMode при разработке)
        // Ничего не делаем, чтобы не сбросить текущее состояние.
        console.log(`[LOAD EFFECT] Данные для fileId ${fileId} уже загружены. Пропускаем повторный запуск.`);
        return;
    }


    const loadOrCreateNote = async () => {
      if (loadingFileIdRef.current !== fileId) {
        console.warn(`[LOAD EFFECT] Aborted loadOrCreateNote: fileId изменился (${loadingFileIdRef.current} != ${fileId})`);
        return;
      }
      try {
        console.log(`[LOAD EFFECT] Загрузка/создание для fileId: ${fileId}`);
        let so = await getSuperObjectByFileId(fileId);
        let blocksFromDb: ContentBlock[] = [];

        if (!so) {
          console.log(`[LOAD EFFECT] SuperObject для fileId ${fileId} не найден. Создаем новый.`);
          const newSOData: SuperObject = { fileId: fileId, serviceType: 'document', name: `Заметка ${fileId}` };
          const createdSO = await createSuperObject(newSOData);
          if (!createdSO || !createdSO.id) {
            throw new Error('Не удалось создать SuperObject на сервере.');
          }
          so = createdSO;
          // Для нового SO, блоки будут пустыми (или с дефолтным параграфом, если так решил convert...Data)
          // Важно: после создания SO и до первого сохранения блоков, они не имеют серверных ID.
          // Editor.js сам сгенерирует временные ID.
          // Первый sync отправит их на сервер, и сервер присвоит им постоянные ID.
        } else {
          console.log(`[LOAD EFFECT] SuperObject для fileId ${fileId} найден. Загружаем блоки.`);
          blocksFromDb = await getAllContentBlocksForSuperObject(so);
        }
        
        setCurrentSuperObject(so);
        const initialDataForEditor = convertContentBlocksToEditorJsData(blocksFromDb);
        setEditorInitialDataUsed(initialDataForEditor); // Этот стейт будет использован для initialData пропа
        latestEditorDataRef.current = initialDataForEditor; // Синхронизируем latest с initial при загрузке

        console.log(`[LOAD EFFECT] Успешно загружены данные для fileId: ${fileId}.`);

      } catch (err: any) {
        console.error(`[LOAD EFFECT] Ошибка при загрузке/создании для fileId ${fileId}:`, err);
        if (loadingFileIdRef.current === fileId) setError(err.message || 'Ошибка при загрузке данных заметки.');
      } finally {
        if (loadingFileIdRef.current === fileId) {
          setIsLoading(false);
          // Не сбрасываем loadingFileIdRef.current = null здесь, чтобы следующий useEffect для того же noteId не сработал,
          // если он не должен (например, при hot reload)
        }
      }

    };

    if (isLoading && loadingFileIdRef.current === fileId) {
      loadOrCreateNote();
    }
    
    // Функция очистки не нужна для AbortController здесь, 
    // так как мы управляем через loadingFileIdRef и isLoading.
    // Если бы использовали AbortController, здесь была бы отмена.

  }, [noteId]); // Зависимость только от noteId

  // Debounced функция сохранения
  const debouncedSave = useCallback(
    debounce(async () => {
      const soToSave = currentSuperObjectRefForDebounce.current;
      const editorDataToSave = latestEditorDataRef.current;

      // ... (проверки soToSave, editorDataToSave) ...
      if (!soToSave || !soToSave.id || !editorDataToSave ) {
        console.log('[DEBOUNCED SAVE] Нет SO, ID или данных для сохранения.', { soToSave, editorDataToSave });
        return; 
      }
      // Если блоков нет, все равно сохраняем (пустой массив)
      if (editorDataToSave.blocks.length === 0) {
        console.log('[DEBOUNCED SAVE] Сохранение пустого массива блоков.');
      }


      console.log('[DEBOUNCED SAVE] Попытка сохранить для SO ID:', soToSave.id);
      const payload = convertEditorJsBlocksToPayload(editorDataToSave.blocks);
      
      try {
        const updatedSOFromServer = await syncDocumentBlocksApi(soToSave.id, payload);
        console.log('[DEBOUNCED SAVE] Сервер ответил:', updatedSOFromServer);

        // Аккуратно обновляем СТЕЙТ currentSuperObject, чтобы не вызывать лишних перерисовок Wrapper'а
        // если ID не изменился.
        setCurrentSuperObject(prevSO => {
          if (prevSO && prevSO.id === updatedSOFromServer.id) {
            // Сравните только те поля, которые действительно могли измениться и важны для UI вне редактора
            // (name, lastChangeDate и т.д.). Не создавайте новый объект, если ничего не изменилось.
            const importantFieldsChanged = 
                prevSO.name !== updatedSOFromServer.name ||
                prevSO.lastChangeDate !== updatedSOFromServer.lastChangeDate || // Если это поле важно для UI
                prevSO.firstItem !== updatedSOFromServer.firstItem ||
                prevSO.lastItem !== updatedSOFromServer.lastItem;

            if (importantFieldsChanged) {
              console.log("[DEBOUNCED SAVE] Обновляем стейт currentSuperObject, т.к. важные поля изменились.");
              return { ...prevSO, ...updatedSOFromServer };
            } else {
              console.log("[DEBOUNCED SAVE] Стейт currentSuperObject не требует обновления (важные поля не изменились).");
              // ВАЖНО: Если сервер вернул новые ID для блоков, то `latestEditorDataRef.current`
              // все еще содержит старые (клиентские) ID. Это может быть проблемой при следующем save.
              // Идеально, если `syncDocumentBlocksApi` возвращает `OutputData` с серверными ID
              // и мы обновляем `latestEditorDataRef.current = newOutputDataFromServer;`
              // А затем, если нужно, делаем `editorJsInstanceApiRef.current?.render(newOutputDataFromServer);`
              // НО! Это очень опасно и может сбросить фокус/каретку.
              // Лучше, если ID блоков не меняются после первого сохранения, а только создаются для новых.
              return prevSO; // Возвращаем предыдущий объект, чтобы не триггерить React
            }
          }
          return prevSO; // Если что-то пошло не так с ID
        });
        
        // НЕ ДЕЛАЕМ: setEditorInitialDataUsed(...) - это вызовет откат!
        // НЕ ДЕЛАЕМ: getAllContentBlocksForSuperObject(...) - редактор - источник правды!

      } catch (e: any) {
        console.error('[DEBOUNCED SAVE] Ошибка синхронизации:', e);
        setError(`Ошибка сохранения: ${e.message || 'Неизвестная ошибка'}`);
      }
    }, 2000),
  []);


  const handleEditorChange = useCallback((api: EditorJSAPI, newData: OutputData) => {
    latestEditorDataRef.current = newData;
    debouncedSave();
  }, [debouncedSave]);

  const handleEditorReady = useCallback((editor: EditorJS) => { // Изменил параметр на editor
    editorJsInstanceRef.current = editor; // Сохраняем API или весь editor
    console.log("[NOTE EDITOR WIDGET] EditorJsWrapper готов.");
    // При первой готовности редактора, его данные (initialData) должны быть в latestEditorDataRef
    // Это сделано в useEffect загрузки.
  }, []);


  if (isLoading) return <div><p>Загрузка данных заметки...</p></div>;
  if (error) return <div><p>Ошибка: {error}</p></div>;
  if (!currentSuperObject || !currentSuperObject.id) return <div><p>Заметка не найдена или не удалось инициализировать.</p></div>;
  if (editorInitialDataUsed === undefined) return <div><p>Инициализация редактора...</p></div>;

  return (
    <div className="note-editor-widget-container">
      {currentSuperObject.name && <h2>{currentSuperObject.name}</h2>}
      <EditorJsWrapper
        key={currentSuperObject.id.toString()} // Ключ для перемонтирования при смене SO ID
        holderId={`editorjs-holder-${currentSuperObject.id}`}
        initialData={editorInitialDataUsed} // Только для ПЕРВОЙ инициализации этого инстанса
        onChange={handleEditorChange}
        onReady={handleEditorReady}
        readOnly={false}
      />
    </div>
  );
};
